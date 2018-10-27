package com.pinyougou.service;

import com.pinyougou.pojo.TbSeller;
import com.pinyougou.sellergoods.service.SellerService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;

/**
 * 描述:
 *  认证类
 * @author hudongfei
 * @create 2018-10-24 20:34
 */
public class UserDetailsServiceImpl implements UserDetailsService {

    private SellerService sellerService;

    public SellerService getSellerService() {
        return sellerService;
    }

    public void setSellerService(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    /**
     * Locates the user based on the username. In the actual implementation, the search
     * may possibly be case sensitive, or case insensitive depending on how the
     * implementation instance is configured. In this case, the <code>UserDetails</code>
     * object that comes back may have a username that is of a different case than what
     * was actually requested..
     *
     * @param username the username identifying the user whose data is required.
     * @return a fully populated user record (never <code>null</code>)
     * @throws UsernameNotFoundException if the user could not be found or the user has no
     *                                   GrantedAuthority
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("经过了UserDetailsServiceImpl");

        //构建一个角色列表
        List<GrantedAuthority> grantAuths = new ArrayList<>();
        grantAuths.add(new SimpleGrantedAuthority("ROLE_SELLER"));

        //得到商家对象
        TbSeller seller = sellerService.findOne(username);

        if (seller != null){
            if (seller.getStatus().equals("1")){
                return new User(username,seller.getPassword(),grantAuths);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
}
