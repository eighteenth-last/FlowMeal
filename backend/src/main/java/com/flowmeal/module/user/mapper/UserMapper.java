package com.flowmeal.module.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.flowmeal.module.user.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {}
