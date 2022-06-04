/**
 *    Copyright 2009-2020 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.parsing;

/**
 * 普通记号解析器，处理#{}和${}参数
 * @author Clinton Begin
 */
public class GenericTokenParser {
  // 开始记号
  private final String openToken;
  // 结束记号
  private final String closeToken;
  // 处理器
  private final TokenHandler handler;

  public GenericTokenParser(String openToken, String closeToken, TokenHandler handler) {
    this.openToken = openToken;
    this.closeToken = closeToken;
    this.handler = handler;
  }

  public String parse(String text) {
    // 如果为空,直接返回不做处理
    if (text == null || text.isEmpty()) {
      return "";
    }
    // 匹配开始记号`#{`,`${`
    // search open token
    int start = text.indexOf(openToken);
    if (start == -1) {
      // 说明传入文本中不存在记号,无需处理直接返回
      return text;
    }
    char[] src = text.toCharArray();
    // 偏移量
    int offset = 0;
    // builder是最终结果
    final StringBuilder builder = new StringBuilder();
    // expression用来暂存中间的表达式
    StringBuilder expression = null;
    // 开始之前要明确
    // StringBuilder#append(char[] str, int offset, int len)的作用是
    // 从str的第offset各开始去len长度追加到builder中
    // offset从0开始表示从开头开始
    // len表示长度，0表示空
    do {
      // 如果text中在openToken前存在转义符就将转义符去掉。如果openToken前存在转义符，start的值必然大于0，最小也为1
      // 因为此时openToken是不需要进行处理的，所以也不需要处理endToken。接着查找下一个openToken
      // src[start - 1] == '\\' 一个char回合两个char('\\'是两个char吗？)相等
      // 因为backslash(反斜杠)会转义,所以它会成对出现表示一个backslash
      if (start > 0 && src[start - 1] == '\\') {
        // this open token is escaped. remove the backslash(反斜杠) and continue.
        // 如果opentoken前面有backslash那么就不把这个当作记号处理,去除backslash后跳过opentoken接着进行后续处理
        builder.append(src, offset, start - offset - 1).append(openToken);
        // 重设偏移量，跳过opentoken
        offset = start + openToken.length();
      } else {
        // found open token. let's search close token.
        // 每一次进入这里的时候,expression都应该是新的
        // 这里为了减少循环中创建的变量,就复用了expression
        if (expression == null) {
          expression = new StringBuilder();
        } else {
          expression.setLength(0);
        }
        // 把src中从offset开始一直到opentoken出现位置中间的内容追加到builer中
        builder.append(src, offset, start - offset);
        // 跳过已经搜到的opentoken
        offset = start + openToken.length();
        // 从offset处搜索closetoken位置
        int end = text.indexOf(closeToken, offset);
        while (end > -1) {
          if (end > offset && src[end - 1] == '\\') {
            // 说明closetoken前面存在backslash,需要把opentoken(不包含)至closetoken(包含)的中间内容追加到expression中并跳过这个closetoken
            // this close token is escaped. remove the backslash and continue.
            expression.append(src, offset, end - offset - 1).append(closeToken);
            // 跳过当前closetoken(前一个字符是backslash)
            offset = end + closeToken.length();
            // 从搜到的位置往后继续查询closetoken
            end = text.indexOf(closeToken, offset);
          } else {
            // 一切正常,说明正常匹配了closetoken
            // 把opentoken和closetoken中间的内容追加到表达式中
            expression.append(src, offset, end - offset);
            break;
          }
        }
        if (end == -1) {
          // 说明传入文本只有opentoken没有closetoken,把后面的内容追加到最终结果里
          // close token was not found.
          builder.append(src, start, src.length - start);
          // 偏移量是整个字符串的长度,表明搜索结束
          offset = src.length;
        } else {
          // 调用处理器获取expression代表的值
          builder.append(handler.handleToken(expression.toString()));
          // 重置偏移量，跳过已匹配的值
          offset = end + closeToken.length();
        }
      }
      // 在后续位置继续搜索openToken
      start = text.indexOf(openToken, offset);
    } while (start > -1);// start > -1说明又搜到了opentoken,此时需要再次执行一遍do中的逻辑
    if (offset < src.length) {
      // 如果上面循环结束,offset一般是要大于等于传入字符长度的
      // 出现小于一定是后续字符不存在token,直接追缴到结果中
      builder.append(src, offset, src.length - offset);
    }
    return builder.toString();
  }
}
