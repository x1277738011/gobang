package com.gobang.controller;

import com.gobang.model.User;
import com.gobang.service.UserService;
import com.gobang.tools.AppVariable;
import com.gobang.tools.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@RestController
@RequestMapping("/gobang")
public class UserController {
    @Autowired
    private UserService userService;

    @RequestMapping("/reg")
    public R Register(User user) {
        //非空效验
        if (user == null || !StringUtils.hasLength(user.getUsername()) || !StringUtils.hasLength(user.getPassword())) {
            return R.fail(400, "非法参数");
        }
        if (userService.selectByName(user.getUsername()) != null) {
            return R.success(-1);
        }
        String password = user.getPassword();
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        user.setPassword(bCryptPasswordEncoder.encode(password));
        return R.success(200,userService.save(user));
    }

    @RequestMapping("/login")
    public R Login(HttpServletRequest request, String username, String password) {
        //非空校验
        if (!StringUtils.hasLength(username) || !StringUtils.hasLength(password)) {
            return R.fail(-1, "非法参数");
        }
        BCryptPasswordEncoder bCryptPasswordEncoder =new BCryptPasswordEncoder();
        User user = userService.selectByName(username);
        if (user !=null && user.getUserId()>0){
            if (bCryptPasswordEncoder.matches(password,user.getPassword())){
                user.setPassword("");
                request.getSession().setAttribute(AppVariable.USER_SESSION_KEY, user);
                return R.success(200,user);
            }
        }
        return R.success(0, null);

    }
    @RequestMapping("/logout")
    public R logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        // 拦截器的拦截, 所以不可能出现session为空的情况
        session.removeAttribute(AppVariable.USER_SESSION_KEY);
        return R.success(1);

    }
    @RequestMapping("/userinfo")
    public Object getUserInfo(HttpServletRequest request){
        try {
            HttpSession session = request.getSession(false);
            User user = (User)session.getAttribute("user");
            User newUser = userService.selectByName(user.getUsername());
            return newUser;
        } catch (NullPointerException e) {
            e.printStackTrace();
            return new User();
        }
    }

}
