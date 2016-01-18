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

package org.apache.ibatis.submitted.typehandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

public class Product {

  private ProductId id;

  private String name;

  public ProductId getId() {
    return id;
  }

  public void setId(ProductId id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public static class ProductId {
    private Integer value;

    private ProductId(Integer value) {
      super();
      this.value = value;
    }

    public Integer getValue() {
      return value;
    }
  }

  public static class ProductIdTypeHandler extends BaseTypeHandler<ProductId> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, ProductId parameter, JdbcType jdbcType) throws SQLException {
      ps.setInt(i, parameter.getValue());
    }

    @Override
    public ProductId getNullableResult(ResultSet rs, String columnName) throws SQLException {
      return new ProductId(rs.getInt(columnName));
    }

    @Override
    public ProductId getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
      return new ProductId(rs.getInt(columnIndex));
    }

    @Override
    public ProductId getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
      return new ProductId(cs.getInt(columnIndex));
    }
  }
}