package telran.java52.post.dao;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import telran.java52.post.model.Post;

public interface PostRepository extends MongoRepository<Post, String> {

	Stream<Post> findByAuthorIgnoreCase(String author);
	
	Stream<Post> findByTagsInIgnoreCase(List<String> tags);
	
	@Query("{'dateCreated' : {'$gte': ?0, '$lte': ?1}}")
	Stream<Post> findByDateCreatedBetween(LocalDateTime dateFrom, LocalDateTime dateTo);
}
