package telran.java52.security.filter;

import java.io.IOException;
import java.security.Principal;

import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import telran.java52.accounting.dao.UserAccountRepository;
import telran.java52.accounting.dto.exeptions.UserNotFoundExeption;
import telran.java52.accounting.model.Role;
import telran.java52.accounting.model.UserAccount;

@Component
@RequiredArgsConstructor
@Order(20)
public class AdminManagingRolesFIlter implements Filter {

	final UserAccountRepository userAccountRepository;
	
	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;
		
		if (isChangeRoleEndpoint(request.getMethod(), request.getServletPath())) {
			try {
				Principal userPrincipal = request.getUserPrincipal();
				String login = userPrincipal.getName();
				UserAccount userAccount = userAccountRepository.findById(login).orElseThrow(UserNotFoundExeption::new);
				
				if (!userAccount.getRoles().contains(Role.ADMINISTRATOR)) {
					response.sendError(403);
					return;
				}
			} catch (UserNotFoundExeption e) {
				throw new UserNotFoundExeption();
			} 
		}
		chain.doFilter(request, response);

	}
	
	private boolean isChangeRoleEndpoint(String method, String path) {
                return (HttpMethod.PUT.matches(method) || HttpMethod.DELETE.matches(method)) 
                && path.matches("/account/user/[^/]+/role/[^/]+$");
    }

}
