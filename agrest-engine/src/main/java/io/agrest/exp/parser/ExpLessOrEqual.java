/* Generated By:JJTree: Do not edit this line. ExpLessOrEqual.java Version 7.0 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=Exp,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package io.agrest.exp.parser;

public
class ExpLessOrEqual extends SimpleNode {
  public ExpLessOrEqual(int id) {
    super(id);
  }

  public ExpLessOrEqual(AgExpressionParser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public <T> T jjtAccept(AgExpressionParserVisitor<T> visitor, T data) {

    return
    visitor.visit(this, data);
  }
}
/* JavaCC - OriginalChecksum=12a64aba7c63644dacaacfc213e4645b (do not edit this line) */
