/* Generated By:JJTree: Do not edit this line. ExpLess.java Version 7.0 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=Exp,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package io.agrest.exp.parser;

public
class ExpLess extends SimpleNode {
  public ExpLess(int id) {
    super(id);
  }

  public ExpLess(AgExpressionParser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public <T> T jjtAccept(AgExpressionParserVisitor<T> visitor, T data) {

    return
    visitor.visit(this, data);
  }
}
/* JavaCC - OriginalChecksum=bef1ed42ba71197780cac56ab9b90b36 (do not edit this line) */
