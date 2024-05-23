package telran.java52.security.filter;

import java.io.IOException;

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
import telran.java52.accounting.model.Role;
import telran.java52.accounting.model.UserAccount;
import telran.java52.post.dao.PostRepository;
import telran.java52.post.model.Post;

@Component
@RequiredArgsConstructor
@Order(60)
public class DeletePostFilter implements Filter {
	
	final UserAccountRepository userAccountRepository;
	final PostRepository postRepository;
	
	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;
        
        if(checkEndpoint(request.getMethod(), request.getServletPath())) {
        	String principal = request.getUserPrincipal().getName();
        	String[] parts = request.getServletPath().split("/");
            String postId = parts[parts.length - 1];
            Post post = postRepository.findById(postId).orElse(null);
            UserAccount userAccount = userAccountRepository.findById(principal).orElse(null);
            if(post == null || userAccount == null) {
            	response.sendError(404, "Not found");
            	return;
            }
            if (!(userAccount.getRoles().contains(Role.MODERATOR) || principal.equals(post.getAuthor()))) {
				response.sendError(403, "Permission denied");
				return;
			}
        }
        chain.doFilter(request, response);
		
	}

	private boolean checkEndpoint(String method, String path) {
		return HttpMethod.DELETE.matches(method) && path.matches("forum/post/\\w+");
	}

}
