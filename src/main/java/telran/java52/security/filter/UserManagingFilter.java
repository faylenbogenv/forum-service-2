package telran.java52.security.filter;

import java.io.IOException;
import java.security.Principal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
@Order(30)
@RequiredArgsConstructor
public class UserManagingFilter implements Filter {

	final UserAccountRepository userAccountRepository;

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;

		try {
			if (checkForDeleteUser(request.getMethod(), request.getServletPath())) {
				if (!handleDeleteUser(request, response)) {
					return;
				}
			} else if (checkForUpdateUser(request.getMethod(), request.getServletPath())) {
				if (!handleUpdateUser(request, response)) {
					return;
				}
			}
		} catch (UserNotFoundExeption e) {
			throw new UserNotFoundExeption();
		}

		chain.doFilter(request, response);
	}

	private boolean handleDeleteUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
		Principal userPrincipal = request.getUserPrincipal();
		String login = userPrincipal.getName();
		UserAccount userAccount = userAccountRepository.findById(login).orElseThrow(UserNotFoundExeption::new);
		String targetUser = extractUserName(request.getServletPath());
		if (!(login.equals(targetUser) || userAccount.getRoles().contains(Role.ADMINISTRATOR))) {
			response.sendError(403);
			return false;
		}
		return true;
	}

	private boolean handleUpdateUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
		Principal userPrincipal = request.getUserPrincipal();
		String login = userPrincipal.getName();
		String targetUser = login;
		if (!isPasswordChange(request.getServletPath())) {
			targetUser = extractUserName(request.getServletPath());
		}
		if (!login.equals(targetUser)) {
			response.sendError(403);
			return false;
		}
		return true;
	}

	private String extractUserName(String path) {
		Pattern pattern = Pattern.compile("^/account/user/([^/]+)$");
		Matcher matcher = pattern.matcher(path);
		if (matcher.find()) {
			return matcher.group(1);
		}
		return null;
	}

	private boolean checkForUpdateUser(String method, String path) {
		return HttpMethod.PUT.matches(method) && (path.matches("^/account/user/[^/]+$") || isPasswordChange(path));
	}

	private boolean isPasswordChange(String path) {
		return path.matches("^/account/password$");
	}

	private boolean checkForDeleteUser(String method, String path) {
		return HttpMethod.DELETE.matches(method) && path.matches("^/account/user/[^/]+$");
	}
}

