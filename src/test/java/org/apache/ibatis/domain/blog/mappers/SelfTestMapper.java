package org.apache.ibatis.domain.blog.mappers;

import org.apache.ibatis.domain.blog.Author;

import java.util.List;

public interface SelfTestMapper {
  List<Author> selectAllAuthors(List<String> orderConditions);
}
