package de.peeeq.wurstio.languageserver.requests;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import de.peeeq.wurstio.CompiletimeFunctionRunner;
import de.peeeq.wurstio.WurstCompilerJassImpl;
import de.peeeq.wurstio.gui.WurstGuiImpl;
import de.peeeq.wurstio.languageserver.ModelManager;
import de.peeeq.wurstio.map.importer.ImportFile;
import de.peeeq.wurstio.mpq.MpqEditor;
import de.peeeq.wurstio.mpq.MpqEditorFactory;
import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.WImport;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.ast.WurstModel;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.jassAst.JassProg;
import de.peeeq.wurstscript.jassprinter.JassPrinter;
import de.peeeq.wurstscript.parser.WPos;
import de.peeeq.wurstscript.translation.imtranslation.FunctionFlagEnum;
import de.peeeq.wurstscript.utils.LineOffsets;
import de.peeeq.wurstscript.utils.Utils;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by peter on 16.05.16.
 */
public class RunMap extends UserRequest {

    private final String wc3Path;
    private final File map;
    private final List<String> compileArgs;
    private final String workspaceRoot;
    /** makes the compilation slower, but more safe by discarding results from the editor and working on a copy of the model */
    private SafetyLevel safeCompilation = SafetyLevel.QuickAndDirty;

    static enum SafetyLevel {
        QuickAndDirty, KindOfSafe
    }

    public RunMap(int requestNr, String workspaceRoot, String wc3Path, File map, List<String> compileArgs) {
        super(requestNr);
        this.workspaceRoot = workspaceRoot;
        this.wc3Path = wc3Path;
        this.map = map;
        this.compileArgs = compileArgs;
    }

    @Override
    public Object execute(ModelManager modelManager) {

        // TODO use normal compiler for this, avoid code duplication
        WLogger.info("runMap " + map.getAbsolutePath() + " " + compileArgs);
        WurstGui gui = new WurstGuiImpl(workspaceRoot);
        try {
            File frozenThroneExe = new File(wc3Path, "Frozen Throne.exe");

            if (!map.exists()) {
                throw new RuntimeException(map.getAbsolutePath() + " does not exist.");
            }

            gui.sendProgress("Copying map");

            // first we copy in same location to ensure validity
            File buildDir = getBuildDir();
            File testMap = new File(buildDir, "WurstRunMap.w3x");
            if (testMap.exists()) {
                testMap.delete();
            }
            Files.copy(map, testMap);

            // first compile the script:
            File compiledScript = compileScript(gui, modelManager, compileArgs, testMap, map);

            WurstModel model = modelManager.getModel();
            if (model == null || !model.stream().anyMatch((CompilationUnit cu) -> cu.getFile().endsWith("war3map.j"))) {
                println("No 'war3map.j' file could be found");
                println("If you compile the map with WurstPack once, this file should be in your wurst-folder. ");
                println("We will try to start the map now, but it will probably fail. ");
            }

            @SuppressWarnings("unused") // for side effects!
            RunArgs runArgs = new RunArgs(compileArgs);

            gui.sendProgress("preparing testmap ... ");
            
            
            // then inject the script into the map
            File outputMapscript = compiledScript;

            gui.sendProgress("Injecting mapscript");
            try (MpqEditor mpqEditor = MpqEditorFactory.getEditor(testMap)) {
                mpqEditor.deleteFile("war3map.j");
                mpqEditor.insertFile("war3map.j", Files.toByteArray(outputMapscript));
            }
            

            String testMapName2 = copyToWarcraftMapDir(testMap);

            WLogger.info("Starting wc3 ... ");

            // now start the map
            List<String> cmd = Lists.newArrayList(frozenThroneExe.getAbsolutePath(), "-window", "-loadfile", "Maps\\Test\\" + testMapName2);

            if (!System.getProperty("os.name").startsWith("Windows")) {
                // run with wine
                cmd.add(0, "wine");
            }

            gui.sendProgress("running " + cmd);
            Process p = Runtime.getRuntime().exec(cmd.toArray(new String[0]));
        } catch (CompileError e) {
            e.printStackTrace();
            return "There was an error when compiling the map: " + e.getMessage();
        } catch (final Throwable e) {
            e.printStackTrace();
            WLogger.severe(e);
            return "There was a Wurst bug, while compiling the map: " + e.getMessage();
        } finally {
            gui.sendFinished();
        }
        return "ok"; // TODO
    }

    /**
     * Copies the map to the wc3 map directory
     * 
     *  This directory depends on warcraft version and whether we are on windows or wine is used.
     */
    private String copyToWarcraftMapDir(File testMap) throws IOException {
        String documentPath = FileSystemView.getFileSystemView().getDefaultDirectory().getPath() + File.separator + "Warcraft III";
        if (!new File(documentPath).exists()) {
        	WLogger.info("Warcraft folder " + documentPath + " does not exist.");
        	// Try wine default:
        	documentPath = System.getProperty("user.home") 
        		+ "/.wine/drive_c/users/" + System.getProperty("user.name")+ "/My Documents/Warcraft III";
        	if (!new File(documentPath).exists()) {
        		WLogger.severe("Wine Warcraft folder " + documentPath + " does not exist.");
        	}
        }
        
        
        // 1.27 and lower compat
        if (!(new File(wc3Path, "BlizzardPrepatch.exe")).exists()) {
            print("Version 1.27 or lower detected, changing file location");
            documentPath = wc3Path;
        }
        

        // Then we make a second copy named appropriately
        String testMapName2 = "WurstTestMap.w3x";
        File testMap2 = new File(new File(documentPath, "Maps" + File.separator + "Test"), testMapName2);
        Files.copy(testMap, testMap2);
        return testMapName2;
    }

    private void print(String s) {
        WLogger.info(s);
    }

    private void println(String s) {
        WLogger.info(s);
    }

    private File compileScript(WurstGui gui, ModelManager modelManager, List<String> compileArgs, File mapCopy, File origMap) throws Exception {
        RunArgs runArgs = new RunArgs(compileArgs);
        print("Compile Script : ");
        for (File dep : modelManager.getDependencyWurstFiles()) {
            WLogger.info("dep: " + dep.getPath());
        }

        File war3mapFile = new File(new File(new File(workspaceRoot), "wurst"), "war3map.j");

        //  try to get war3map.j from the map:
        byte[] mapScript;
        try (MpqEditor mpqEditor = MpqEditorFactory.getEditor(mapCopy)) {
            mapScript = mpqEditor.extractFile("war3map.j");
        }
        if (new String(mapScript, StandardCharsets.UTF_8).startsWith(JassPrinter.WURST_COMMENT_RAW)) {
            // file generated by wurst, do not use
            if (war3mapFile.exists()) {
                WLogger.info(
                        "Cannot use war3map.j from map file, because it already was compiled with wurst. " + "Using war3map.j from Wurst directory instead.");
            } else {
                CompileError err = new CompileError(new WPos(mapCopy.toString(), new LineOffsets(), 0, 0),
                        "Cannot use war3map.j from map file, because it already was compiled with wurst. " + "Please add war3map.j to the wurst directory.");
                gui.showInfoMessage(err.getMessage());
                throw err;
            }
        } else {
            // write mapfile from map to workspace
            Files.write(mapScript, war3mapFile);
        }

        // push war3map.j to modelmanager

        modelManager.syncCompilationUnit(war3mapFile.getAbsolutePath());

        if (safeCompilation != SafetyLevel.QuickAndDirty) {
            // it is safer to rebuild the project, instead of taking the current editor state
            gui.sendProgress("Cleaning project");
            modelManager.clean();
            gui.sendProgress("Building project");
            modelManager.buildProject();
        }

        if (modelManager.hasErrors()) {
            throw new RuntimeException("Model has errors");
        }

        WurstModel model = modelManager.getModel();
        if (safeCompilation != SafetyLevel.QuickAndDirty) {
            // compilation will alter the model (e.g. remove unused imports), 
            // so it is safer to create a copy
            model = model.copy();
        }

        MpqEditor mpqEditor = null;
        if (mapCopy != null) {
            mpqEditor = MpqEditorFactory.getEditor(mapCopy);
        }

        //WurstGui gui = new WurstGuiLogger();

        WurstCompilerJassImpl compiler = new WurstCompilerJassImpl(gui, mpqEditor, runArgs);
        compiler.setMapFile(mapCopy);
        purgeUnimportedFiles(model);

        gui.sendProgress("Check program");
        compiler.checkProg(model);

        if (gui.getErrorCount() > 0) {
            print("Could not compile project\n");
            System.err.println("Could not compile project: " + gui.getErrorList().get(0));
            throw new RuntimeException("Could not compile project: " + gui.getErrorList().get(0));
        }

        print("translating program ... ");
        compiler.translateProgToIm(model);

        if (gui.getErrorCount() > 0) {
            print("Could not compile project (error in translation)\n");
            System.err.println("Could not compile project (error in translation): " + gui.getErrorList().get(0));
            throw new RuntimeException("Could not compile project (error in translation): " + gui.getErrorList().get(0));
        }

        if (runArgs.runCompiletimeFunctions()) {
            print("running compiletime functions ... ");
            // compile & inject object-editor data
            // TODO run optimizations later?
            gui.sendProgress("Running compiletime functions");
            CompiletimeFunctionRunner ctr = new CompiletimeFunctionRunner(compiler.getImProg(), compiler.getMapFile(), compiler.getMapfileMpqEditor(), gui,
                    FunctionFlagEnum.IS_COMPILETIME);
            ctr.setInjectObjects(runArgs.isInjectObjects());
            ctr.setOutputStream(new PrintStream(System.out));
            ctr.run();
        }

        if (runArgs.isInjectObjects()) {
            Preconditions.checkNotNull(mpqEditor);
            // add the imports
            ImportFile.importFilesFromImportDirectory(origMap, mpqEditor);
        }

        print("translating program to jass ... ");
        compiler.transformProgToJass();

        JassProg jassProg = compiler.getProg();
        if (jassProg == null) {
            print("Could not compile project\n");
            throw new RuntimeException("Could not compile project (error in JASS translation)");
        }

        gui.sendProgress("Printing program");
        JassPrinter printer = new JassPrinter(true, jassProg);
        String compiledMapScript = printer.printProg();

        File buildDir = getBuildDir();
        File outFile = new File(buildDir, "compiled.j.txt");
        Files.write(compiledMapScript.getBytes(Charsets.UTF_8), outFile);
        return outFile;
    }

    private File getBuildDir() {
        File buildDir = new File(workspaceRoot, "_build");
        buildDir.mkdirs();
        return buildDir;
    }

    /**
     * removes everything compilation unit which is neither
     * - inside a wurst folder
     * - a jass file
     * - imported by a file in a wurst folder
     */
    private void purgeUnimportedFiles(WurstModel model) {
        Set<CompilationUnit> inWurstFolder =
                model.stream().filter(cu -> isInWurstFolder(cu.getFile()) || cu.getFile().endsWith(".j")).collect(Collectors.toSet());

        Set<CompilationUnit> imported = new HashSet<>(inWurstFolder);
        addImports(imported, imported);

        model.removeIf(cu -> !imported.contains(cu));
    }

    private void addImports(Set<CompilationUnit> result, Set<CompilationUnit> toAdd) {
        Set<CompilationUnit> imported =
                toAdd.stream().flatMap((CompilationUnit cu) -> cu.getPackages().stream()).flatMap((WPackage p) -> p.getImports().stream())
                        .map(WImport::attrImportedPackage).filter(p -> p != null).map(WPackage::attrCompilationUnit).collect(Collectors.toSet());
        boolean changed = result.addAll(imported);
        if (changed) {
            // recursive call terminates, as there are only finitely many compilation units
            addImports(result, imported);
        }
    }

    private boolean isInWurstFolder(String file) {
        Path p = Paths.get(file);
        Path w = Paths.get(workspaceRoot);
        return p.startsWith(w) 
                && java.nio.file.Files.exists(p)
                && Utils.isWurstFile(file);
    }
}
