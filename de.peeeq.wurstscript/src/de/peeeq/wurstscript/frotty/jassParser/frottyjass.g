from 1.24 patchgrammar frottyjass;


@lexer::header {
  package de.peeeq.wurstscript.frotty.jassParser; 
}

@header {
  package de.peeeq.wurstscript.frotty.jassParser; 
  import de.peeeq.wurstscript.jassAst.*;
  import com.google.common.collect.Lists;
  import com.google.common.collect.Maps;
  import static de.peeeq.wurstscript.jassAst.JassAst.*;
  import de.peeeq.wurstscript.utils.*;
  import java.util.Map;
}

@members {
    private List<String> errors = new ArrayList<String>();
    private JassProgs progs;
    private static Map<JassAstElement, Integer> lineMap;
    private static Map<String, JassFunction> functionsMap = Maps.newHashMap();
	  private static Map<String, JassNative> nativesMap = Maps.newHashMap();
	  private static Map<String, JassVar> globalsMap = Maps.newHashMap();
	  private static Map<String, JassTypeDef> typeDefsMap = Maps.newHashMap();
    public void displayRecognitionError(String[] tokenNames,
                                        RecognitionException e) {
        String hdr = getErrorHeader(e);
        String msg = getErrorMessage(e, tokenNames);
        errors.add(hdr + " " + msg);
        //throw new Error(e);
    }
    public List<String> getErrors() {
        return errors;
    }
}



// Globals


file[JassProgs prgs] returns [JassProg prog] : 
  {
    $prog = JassProg(JassTypeDefs(), JassVars(), JassNatives(), JassFunctions());
    prgs.add($prog); 
    lineMap = $prog.attrLineMap();
    progs = prgs;
  }
   
  ( 
  
  NEWLINE*
	  (
	  typDef=typeDefinition
		  {
		    $prog.getDefs().add(typDef);
		  }
	  | globals1=globalsBlock 
		  {
		    // first remove all elements from globals1 before adding them
		    $prog.getGlobals().addAll(globals1.removeAll());
		  }
	  | native_decl=nativeDeclaration
		  {
		    $prog.getNatives().add(native_decl);
		  }
	  )
  )* 
  
  (
  NEWLINE*
  func=function
  {
    $prog.getFunctions().add(func);
    progs.addFunction( func );
  }
  )* 
  NEWLINE* EOF // hier at NEWLINE* gefehlt
  ;

  
typeDefinition returns [JassTypeDef typeDef]
  : 
  (
  'type' name1=ID 'extends' extends1=(ID|'handle') NEWLINE
  {
    $typeDef = JassTypeDef( name1.getText(), extends1.getText() );
    progs.addTypeDef( $typeDef );
  }
  )
  ;

globalsBlock returns [JassVars jvars]
  : 
  'globals' NEWLINE+ globals=global_variables 'endglobals'
  {
    $jvars = globals;
  }
  ;

global_variables returns [JassVars jvars]
  : 
  {
    $jvars = JassVars();
  }
  ( 
  'constant' typ=type name1=ID '=' expr=expression NEWLINE+
  {
    JassVar v = JassConstantVar( typ, name1.getText(), expr );
    $jvars.add( v );
    progs.addGlobal( v );
  }
  | var_decl=variable_declaration NEWLINE+ 
  {
    $jvars.add( var_decl );
    progs.addGlobal( var_decl );
  }
  )*
  ;

nativeDeclaration returns [JassNative jnative]
  : 'constant'? 'native' func_decl=function_declaration
  {
    $jnative = JassNative(func_decl.name, func_decl.params, func_decl.returnType );
    progs.addNative( $jnative );
    JassSimpleVars parms = JassSimpleVars();
    parms.addAll(func_decl.params.removeAll());
    
    progs.addFunction(JassFunction(func_decl.name, parms, func_decl.returnType, JassVars(), JassStatements()));
  }
  ;
  

function returns [JassFunction func]
  : 'constant'? 'function' decl=function_declaration NEWLINE+
    lcls=locals stmts=statements 'endfunction' NEWLINE
    {
      $func = JassFunction(decl.name, decl.params, decl.returnType, lcls, stmts);
      lineMap.put( $func, decl.lineNr );
    }
  ;

function_declaration returns [String name, JassSimpleVars params, String returnType, int lineNr]
  : n=ID
  {
    $name = n.getText();
    $lineNr =  n.getLine();
  }
  
   'takes' 
   (
      'nothing' { $params = JassSimpleVars(); }
      | ps=paramaters {$params = ps; } 
   ) 
   'returns' 
   (
        rt=type { $returnType = rt; }
        | 'nothing' { $returnType = "nothing"; }
   )
  ;

paramaters returns [JassSimpleVars vars]
  : 
  {
    $vars = JassSimpleVars();
  }
  
  t1=type n1=ID 
  { 
    JassSimpleVar var = JassSimpleVar(t1, n1.getText());
    lineMap.put( var, n1.getLine() );
    $vars.add(var); 
  }
  
   (',' t2=type n2=ID  
   { 
    JassSimpleVar var2 = JassSimpleVar(t2, n2.getText());
    lineMap.put( var2, n2.getLine() );
    $vars.add(var2); 
   }
   )*
  ;
  
// Locals
locals returns [JassVars vars]
  : 
  {
    $vars = JassVars();
  }
  (
  'local' var_decl=variable_declaration  NEWLINE+
  {
    $vars.add(var_decl);
  }
  )*
  ;

variable_declaration returns [JassVar jvar]
  : 
  typ=type name1=ID 
  {
    $jvar = JassSimpleVar( typ, name1.getText() );
    lineMap.put($jvar, name1.getLine());
  }
  | typ=type name1=ID '=' expr1=expression
  {
    $jvar = JassInitializedVar( typ, name1.getText(), expr1 );
    lineMap.put($jvar, name1.getLine());
  }
  | typ=type 'array' name1=ID
  {
    $jvar = JassArrayVar( typ, name1.getText() );
    lineMap.put($jvar, name1.getLine());
  }
  ;
//Statements
statements returns [JassStatements statements]
  : 
  {
    $statements = JassStatements();
  }
  (
  stmt2=statement NEWLINE+
  {
    $statements.add(stmt2);
  }
  )*
  ;

statement returns [JassStatement statement]
  : 
  setstmt=s_set
  {
    $statement = setstmt;  
  }
  | callstmt=call 
  
  {
    $statement = callstmt;    
  }
  | ifstmt=s_if 
  {
    $statement = ifstmt;    
  }
  | loopstmt=loop 
  {
    $statement = loopstmt;    
  }
  | exitstmt=exitwhen 
  {
    $statement = exitstmt;    
  }
  | retstmt=s_return // | stmt1=debug
  {
    $statement = retstmt;    
  }
  ;

  s_set returns [JassStatement stmt]
  : 
  'set' name1=ID '=' expr=expression 
  {
    $stmt = JassStmtSet( name1.getText(), expr );
    lineMap.put($stmt, name1.getLine());
  }
  | 'set' name1=ID '[' expr1=expression ']' '=' expr2=expression
  {
    lineMap.put($stmt, name1.getLine());
    $stmt = JassStmtSetArray( name1.getText(), expr1, expr2 );
  }
  ;


  call returns [JassStatement stmt]  
    : 
    'call' name1=ID '(' ')'
    {
	    $stmt = JassStmtCall( name1.getText(), JassExprlist() );
	  }
    | 'call' name1=ID '(' args=arguments ')'
    {
      $stmt = JassStmtCall( name1.getText(), args );
    }
    ;

  s_if returns [JassStmtIf stmt]
    : 
    'if' expr=expression 'then' NEWLINE+ stmts=statements
    {
      $stmt = JassStmtIf( expr, stmts, JassStatements() );
    }
    (
    elseblock=else_clause
    {
      $stmt.setElseBlock(elseblock);       
    }
    )? 'endif'
    ;
    
  else_clause returns [JassStatements stmts]
    : 
    {
      $stmts = JassStatements();
    }
    'else' NEWLINE+ stmts1=statements
    {
      $stmts.addAll(stmts1.removeAll());
    }
    | 'elseif' expr=expression 'then' NEWLINE+ stmts1=statements
    {
      JassStmtIf if2 = JassStmtIf(expr,stmts1, JassStatements());
      
      $stmts = JassStatements();
      $stmts.add(if2);
    }
    ( elseblock=else_clause
    {
      if2.setElseBlock(elseblock);
    }
    )?
    ;
    
  loop returns [JassStatement loop]
    : 
    'loop' NEWLINE+ stmts=statements 'endloop'
    {
      $loop = JassStmtLoop(stmts);
    }
    ;

  exitwhen returns [JassStatement exit]
    : 
    'exitwhen' expr=expression //must be in a loop
    {
      $exit = JassStmtExitwhen(expr);
    }
    ;

  s_return  returns [JassStatement stmt]
    : 
    'return' expr=expression
    {
      $stmt = JassStmtReturn( expr );
    }
    | 'return'
    {
      $stmt = JassStmtReturnVoid();
    }
    ;

//  debug 
//    : 'debug' (s_set|call|s_if|loop)
//    ;


//Expressions

expression returns [JassExpr jexpr] //?
  : andExpr=andExpression
  {
    $jexpr = andExpr;
  } 

//  | cnst=constant
//  {
//    $jexpr = cnst;
//  } 
  
  ;
  
andExpression returns [JassExpr jexpr]
  :   
  left=orExpression 
  {
    $jexpr = left;
  }
  (
    'and' right=orExpression
    {
      $jexpr = JassExprBinary($jexpr, JassOpAnd(), right); 
    }
  )*
  ;
  
orExpression returns [JassExpr jexpr]
  :   
  left=multExpression 
  {
    $jexpr = left;
  }
  (
    'or' right=multExpression
    {
      $jexpr = JassExprBinary($jexpr, JassOpOr(), right); 
    }
    
  )*
  ; 

multExpression returns [JassExpr jexpr]
    :   
    left = miniExpression 
    {
      $jexpr = left;
    }
    ( 
      '==' right=miniExpression 
      {
	      $jexpr = JassExprBinary($jexpr, JassOpEquals(), right); 
	    }      
      | '!=' right=miniExpression  
      {
        $jexpr = JassExprBinary($jexpr, JassOpUnequals(), right); 
      }   
      | '>' right=miniExpression
      {
        $jexpr = JassExprBinary($jexpr, JassOpGreater(), right); 
      } 
      | '<' right=miniExpression
      {
        $jexpr = JassExprBinary($jexpr, JassOpLess(), right); 
      } 
      | '<=' right=miniExpression
      {
        $jexpr = JassExprBinary($jexpr, JassOpLessEq(), right); 
      } 
      | '>=' right=miniExpression
      {
        $jexpr = JassExprBinary($jexpr, JassOpGreaterEq(), right); 
      } 
    )*
    ;

miniExpression returns [JassExpr jexpr] 
  : 
  left=mikroExpression
  {
	  $jexpr = left;
	}
  ( '+' right=mikroExpression 
  {
    $jexpr = JassExprBinary($jexpr, JassOpPlus(), right); 
  } 
  | '-' right=mikroExpression   
  {
    $jexpr = JassExprBinary($jexpr, JassOpMinus(), right); 
  }   
  )*
  ;

mikroExpression returns [JassExpr jexpr] 
  : 
  left=atom 
  {
    $jexpr = left;
  }
  ( '*' right=atom 
  {
    $jexpr = JassExprBinary($jexpr, JassOpMult(), right); 
  }   
  | '/' right=atom
  {
    $jexpr = JassExprBinary($jexpr, JassOpDiv(), right); 
  }  
  )*
  ; 

atom  returns [JassExpr jexpr]
    : 
    varac=ID
    {
      $jexpr = JassExprVarAccess( varac.getText() );
    }
    | '-' right=atom
    {
      $jexpr = JassExprUnary( JassOpMinus(), right );
    }
    | 'not' right=atom
    {
      $jexpr = JassExprUnary( JassOpNot(), right );
    }
    |   '(' expr=expression ')'
    {
      $jexpr = expr;
    }
    |  cnst=constant
    {
      $jexpr = cnst;
    }
    | funcCall=function_call 
	  {
	    $jexpr = funcCall;
	  } 
	  | arrayRef=array_reference 
	  {
	    $jexpr = arrayRef;
	  } 
	  | funcRef=func_reference 
	  {
	    System.out.println("jjJJ");
	    $jexpr = funcRef;
	  } 
    ;
    
function_call returns [JassExprFunctionCall expr]
  : 
  name=ID '(' args=arguments ')'
  {
    $expr = JassExprFunctionCall( name.getText(), args );
  }
  | name=ID '('')'
  {
    $expr = JassExprFunctionCall( name.getText(), JassExprlist() );
  }

  ;

arguments returns [JassExprlist exprs]
  : 
  {
    $exprs = JassExprlist();
  }
  expr1=expression 
  {
    $exprs.add(expr1);
  }
  (
  ',' expr2=expression
  {
    $exprs.add(expr2);
  }
  )*
  ;

array_reference returns [JassExprVarArrayAccess expr]
  : 
  name=ID '[' expr1=expression ']'
  {
    $expr = JassExprVarArrayAccess(name.getText(), expr1);
  }
  ;

  func_reference returns [JassExprFuncRef expr ]
    : 
    'function' name=ID
    {
      $expr = JassExprFuncRef( name.getText() );
    }
    ;

  constant returns [JassExpr jatomic]
    : 
      iconst=int_const
      {
        $jatomic = iconst; 
      } 
      | rconst=Real_const
      {
        $jatomic = JassExprRealVal(rconst.getText()); 
      } 
      | bconst=Bool_const
      {
        $jatomic = bconst.getText().equals("false") ? JassExprBoolVal(false) : JassExprBoolVal(true);
      } 
      | sconst=String_const
      {
        String s = sconst.getText();
        s = s.substring( 1, s.length()-1 );
        StringBuilder result = new StringBuilder();
	      for (int i=0; i<s.length(); i++) {
	        char c = s.charAt(i);
	        if (c == '\\') {
	          i++;
	          c = s.charAt(i);
	          switch (c) {
	            case 'n':
	              result.append("\n");
	              break;
	            case 'r':
	              result.append("\r");
	              break;
	            case 'b':
	              result.append("\b");
	              break;
	            case 't':
	              result.append("\t");
	              break;
	            case 'f':
	              result.append("\f");
	              break;
	            case '\\':
	              result.append("\\");
	              break;
	            case '\"':
	              result.append("\"");
	              break;
	            case '\'':
	              result.append("\'");
	              break;
	            }
	        } else {
	          result.append(c);
	        }
	      }
        $jatomic = JassExprStringVal(result.toString()); 
      } 
      | 'null'
      {
        $jatomic = JassExprNull(); 
      }
    ;

  int_const returns [JassExprIntVal jint]
    : 
    d=Decimal
    {
      $jint = JassExprIntVal(Integer.parseInt(d.getText())); //? :D
    } 
    | o=Octal
    {
      $jint = JassExprIntVal(Integer.parseInt(o.getText(), 8)); //? :D
    } 
    | h=Hex
    {
      String s = h.getText();
      s = s.substring(1, s.length());
      
          
      $jint = JassExprIntVal(Integer.parseInt(s, 16)); //? :D
    } 
    | f=Fourcc
    {
      $jint = JassExprIntVal(Utils.parseAsciiInt4(f.getText())); //? :D Danke
    } 
    ;


    Decimal : ('1'..'9')('0'..'9')*
      ;
      
    Octal : '0'('0'..'7')*
      ;
      
    Hex : '$'('0'..'9'|'a'..'f'|'A'..'F')+ | '0'('x''X') ('0'..'9'|'a'..'f'|'A'..'F')+ 
      ;
      
    Fourcc  : '\'' . . . . '\''
      ;
      
  Real_const
    : ('0'..'9')+ '.' ('0'..'9')* | '.' ('0'..'9')+
    ;

  Bool_const
    : 'true' | 'false'
    ;

  String_const
    :  '"' ( ESC_SEQ | ~('\\'|'"') )* '"'
    ;

//  parenthesis
//    : '(' expression ')'
//    ;

// BaseRegex
type returns [String typeName]  : t=(ID | 'code' | 'handle' | 'integer' | 'real' | 'boolean' | 'string')
  {
    $typeName = t.getText();
  }
  ;
ID    : ('a'..'z'|'A'..'Z') (('a'..'z'|'A'..'Z'|'0'..'9'|'_')* ('a'..'z'|'A'..'Z'|'0'..'9') )?;

NEWLINE : ('\r'|'\n')+  ;
      
//stat:   expression NEWLINE {System.out.println($expression.value);}
//    |   ID '=' expression NEWLINE
//        {memory.put($ID.text, new Integer($expression.value));}
//    |   NEWLINE
//    

COMMENT
   :  '//' ~('\n'|'\r')* {$channel=HIDDEN;}
   ;
    
fragment
ESC_SEQ
    :   '\\' ('b'|'t'|'n'|'f'|'r'|'\"'|'\''|'\\')
    ;


WS  :   ( ' '
        | '\t'
        ) {$channel=HIDDEN;}
    ;








 
