/**
 *    Copyright 2009-2016 the original author or authors.
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

import java.util.Properties;

/**
 * 主要用来解析配置中的动态值
 * 形如: ${driver},${driver:driverDefaultValue}
 * @author Clinton Begin
 * @author Kazuki Shimizu
 */
public class PropertyParser {

  private static final String KEY_PREFIX = "org.apache.ibatis.parsing.PropertyParser.";
  /**
   * The special property key that indicate whether enable a default value on placeholder.
   * <p>
   *   The default value is {@code false} (indicate disable a default value on placeholder)
   *   If you specify the {@code true}, you can specify key and default value on placeholder (e.g. {@code ${db.username:postgres}}).
   * </p>
   * @since 3.4.2
   */
  public static final String KEY_ENABLE_DEFAULT_VALUE = KEY_PREFIX + "enable-default-value";

  /**
   * The special property key that specify a separator for key and default value on placeholder.
   * <p>
   *   The default separator is {@code ":"}.
   * </p>
   * @since 3.4.2
   */
  public static final String KEY_DEFAULT_VALUE_SEPARATOR = KEY_PREFIX + "default-value-separator";

  private static final String ENABLE_DEFAULT_VALUE = "false";
  private static final String DEFAULT_VALUE_SEPARATOR = ":";

  private PropertyParser() {
    // Prevent Instantiation
  }

  public static String parse(String string, Properties variables) {
    VariableTokenHandler handler = new VariableTokenHandler(variables);
    // ${}这个符号就是mybatis配置文件中的占位符
    // 例如定义datasource时用到的 <property name="driverClassName" value="${driver}" />
    // 同时也可以解释在VariableTokenHandler中的handleToken时，如果content在properties中不存在时，返回的内容要加上${}了。
    GenericTokenParser parser = new GenericTokenParser("${", "}", handler);
    return parser.parse(string);
  }

  private static class VariableTokenHandler implements TokenHandler {
    private final Properties variables;
    private final boolean enableDefaultValue;
    // 默认值分隔符,一般是`:`
    private final String defaultValueSeparator;

    private VariableTokenHandler(Properties variables) {
      this.variables = variables;
      this.enableDefaultValue = Boolean.parseBoolean(getPropertyValue(KEY_ENABLE_DEFAULT_VALUE, ENABLE_DEFAULT_VALUE));
      this.defaultValueSeparator = getPropertyValue(KEY_DEFAULT_VALUE_SEPARATOR, DEFAULT_VALUE_SEPARATOR);
    }

    private String getPropertyValue(String key, String defaultValue) {
      return (variables == null) ? defaultValue : variables.getProperty(key, defaultValue);
    }

    /**
     *
     * @param content 这里的content就是去掉`openToken(‘${’)`,`closeToken(‘}’)`后的值 ${name} => name
     *                也有可能是形如`key:default`这种格式
     * @return
     */
    @Override
    public String handleToken(String content) {
      if (variables != null) {
        String key = content;
        // ${key:default} default是默认值
        if (enableDefaultValue) {
          final int separatorIndex = content.indexOf(defaultValueSeparator);
          String defaultValue = null;
          if (separatorIndex >= 0) {
            // 存在默认值
            key = content.substring(0, separatorIndex);
            defaultValue = content.substring(separatorIndex + defaultValueSeparator.length());
          }
          if (defaultValue != null) {
            // 如果variables中不存在对应的值,返回默认值
            return variables.getProperty(key, defaultValue);
          }
        }
        // 如果variables中存在key,则返回对应的值
        if (variables.containsKey(key)) {
          return variables.getProperty(key);
        }
      }
      /**
       * 如果 variables 为空,则返回content,并在content两边追加opentoken与closetoken
       * 疑问: 这里不太准确,因为opentoken与closetoken不一定是`${`与`}`？
       * 原因: VariableTokenHandler是静态私有类
       * org.apache.ibatis.parsing.PropertyParser#parse中定义了opentoken为`${`,closetoken为`}`
       * 所以这里没有问题
       */
      return "${" + content + "}";
    }
  }

}
