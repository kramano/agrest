/* Generated By:JJTree: Do not edit this line. ExpBitwiseLeftShift.java Version 7.0 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=Exp,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package io.agrest.exp.parser;

public
class ExpBitwiseLeftShift extends SimpleNode {
  public ExpBitwiseLeftShift(int id) {
    super(id);
  }

  public ExpBitwiseLeftShift(AgExpressionParser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public <T> T jjtAccept(AgExpressionParserVisitor<T> visitor, T data) {

    return
    visitor.visit(this, data);
  }
}
/* JavaCC - OriginalChecksum=7ae53829a20244620276d32ecd6abb47 (do not edit this line) */
