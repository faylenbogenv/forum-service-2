package telran.java52.post.dto.exeption;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class PostNotFoundExeption extends RuntimeException {

	private static final long serialVersionUID = 1L;

}
