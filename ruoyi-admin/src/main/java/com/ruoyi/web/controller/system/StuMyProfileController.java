package com.ruoyi.web.controller.system;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.system.service.StuMyProfileService;

@RestController
@RequestMapping("/student/profile")
public class StuMyProfileController extends BaseController
{
    @Autowired private StuMyProfileService profileService;

    @PreAuthorize("@ss.hasPermi('student:profile:my')")
    @GetMapping("/my")
    public AjaxResult myProfile()
    {
        return success(profileService.getMyProfile());
    }
}
