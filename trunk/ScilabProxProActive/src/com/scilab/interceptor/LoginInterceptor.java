package com.scilab.interceptor;
import java.util.Map;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;
import com.scilab.pojo.UserInfo;

public class LoginInterceptor extends AbstractInterceptor{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3889063353825736769L;

	@Override
	public String intercept(ActionInvocation ai) throws Exception {
		Map<String,Object> session=ai.getInvocationContext().getSession();
		UserInfo user=(UserInfo)session.get("user");
		if(user.getUserName()!=null&&user.getUserName().length()>0){
			return ai.invoke();
		}
		ActionContext ac=ai.getInvocationContext();
		ac.put("popedom", "您还没有登陆，请登录");
		return Action.LOGIN;
	}
}
