package com.ruoyi.framework.web.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import java.util.concurrent.ScheduledExecutorService;
import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.RegisterBody;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.MessageUtils;
import com.ruoyi.common.utils.spring.SpringUtils;
import com.ruoyi.framework.manager.AsyncManager;
import com.ruoyi.framework.manager.factory.AsyncFactory;
import com.ruoyi.system.mapper.SysRoleMapper;
import com.ruoyi.system.service.ISysConfigService;
import com.ruoyi.system.service.ISysUserService;

@ExtendWith(MockitoExtension.class)
class SysRegisterServiceTest
{
    @Mock ISysUserService userService;
    @Mock ISysConfigService configService;
    @Mock SysRoleMapper roleMapper;
    @InjectMocks SysRegisterService service;

    private RegisterBody body(String role)
    {
        RegisterBody input = new RegisterBody();
        input.setUsername("demo"); input.setPassword("Demo123"); input.setRoleKey(role);
        return input;
    }

    @BeforeEach void setup()
    {
        when(configService.selectCaptchaEnabled()).thenReturn(false);
        when(userService.checkUserNameUnique(any())).thenReturn(true);
    }

    @Test void rejectsAdminAndUnknownRole()
    {
        assertEquals("请选择学生或班主任角色", service.register(body("admin")));
        assertEquals("请选择学生或班主任角色", service.register(body("other")));
        verifyNoInteractions(roleMapper);
        verify(userService, never()).insertUser(any());
    }

    @Test void rejectsDisabledOrMissingRole()
    {
        SysRole role = new SysRole(); role.setRoleId(101L); role.setStatus("1");
        when(roleMapper.checkRoleKeyUnique("student")).thenReturn(role);
        assertEquals("所选角色不可用，请联系管理员", service.register(body("student")));
        verify(userService, never()).insertUser(any());
    }

    @Test void registersOnlySelectedEnabledRoleWithEncryptedPassword()
    {
        SysRole role = new SysRole(); role.setRoleId(100L); role.setStatus("0");
        when(roleMapper.checkRoleKeyUnique("teacher")).thenReturn(role);
        when(userService.insertUser(any())).thenReturn(0);
        assertEquals("注册失败,请联系系统管理人员", service.register(body("teacher")));
        ArgumentCaptor<SysUser> captor = ArgumentCaptor.forClass(SysUser.class);
        verify(userService).insertUser(captor.capture());
        assertArrayEquals(new Long[] {100L}, captor.getValue().getRoleIds());
        assertTrue(SecurityUtils.matchesPassword("Demo123", captor.getValue().getPassword()));
    }

    @Test void studentRegistrationSucceedsWithStudentRole()
    {
        SysRole role = new SysRole(); role.setRoleId(101L); role.setStatus("0");
        when(roleMapper.checkRoleKeyUnique("student")).thenReturn(role);
        when(userService.insertUser(any())).thenReturn(1);
        ConfigurableListableBeanFactory factoryBean = mock(ConfigurableListableBeanFactory.class);
        when(factoryBean.getBean("scheduledExecutorService")).thenReturn(mock(ScheduledExecutorService.class));
        new SpringUtils().postProcessBeanFactory(factoryBean);
        AsyncManager manager = mock(AsyncManager.class);
        try (MockedStatic<AsyncManager> managers = mockStatic(AsyncManager.class);
             MockedStatic<AsyncFactory> factory = mockStatic(AsyncFactory.class);
             MockedStatic<MessageUtils> messages = mockStatic(MessageUtils.class))
        {
            managers.when(AsyncManager::me).thenReturn(manager);
            assertEquals("", service.register(body("student")));
            ArgumentCaptor<SysUser> captor = ArgumentCaptor.forClass(SysUser.class);
            verify(userService).insertUser(captor.capture());
            assertArrayEquals(new Long[] {101L}, captor.getValue().getRoleIds());
        }
        finally
        {
            new SpringUtils().postProcessBeanFactory(null);
        }
    }
}
