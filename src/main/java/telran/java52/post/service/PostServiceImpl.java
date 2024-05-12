package telran.java52.post.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import telran.java52.post.dao.PostRepository;
import telran.java52.post.dto.DatePeriodDto;
import telran.java52.post.dto.NewCommentDto;
import telran.java52.post.dto.NewPostDto;
import telran.java52.post.dto.PostDto;
import telran.java52.post.dto.exeption.PostNotFoundExeption;
import telran.java52.post.model.Comment;
import telran.java52.post.model.Post;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {
	
	final PostRepository postRepository;
	final ModelMapper modelMapper;

	@Override
	public PostDto addNewPost(String author, NewPostDto newPostDto) {
		Post post = modelMapper.map(newPostDto, Post.class);
		post.setAuthor(author);
		post = postRepository.save(post);
		return modelMapper.map(post, PostDto.class);
	}

	@Override
	public PostDto findPostById(String id) {
		Post post = postRepository.findById(id).orElseThrow(PostNotFoundExeption::new);
		return modelMapper.map(post, PostDto.class);
	}

	@Override
	public PostDto removePost(String id) {
		Post post = postRepository.findById(id).orElseThrow(PostNotFoundExeption::new);
		postRepository.delete(post);
		return modelMapper.map(post, PostDto.class);
	}

	@Override
	public PostDto updatePost(String id, NewPostDto newPostDto) {
		Post post = postRepository.findById(id).orElseThrow(PostNotFoundExeption::new);
		if(newPostDto.getTitle() != null) {
			post.setTitle(newPostDto.getTitle());
		}
		if(newPostDto.getContent() != null) {
			post.setContent(newPostDto.getContent());
		}
		Set<String> tags = newPostDto.getTags();
		if(tags != null) {
			tags.forEach(post::addTag);
		}
		post = postRepository.save(post);
		return modelMapper.map(post, PostDto.class);
	}

	@Override
	public PostDto addComment(String id, String author, NewCommentDto newCommentDto) {
		Post post = postRepository.findById(id).orElseThrow(PostNotFoundExeption::new);
		Comment comment = new Comment(author, newCommentDto.getMessage());
		post.addComment(comment);
		post = postRepository.save(post);
		return modelMapper.map(post, PostDto.class);
	}

	@Override
	public void addLike(String id) {
		Post post = postRepository.findById(id).orElseThrow(PostNotFoundExeption::new);
		post.addLikes();
		postRepository.save(post);

	}

	@Override
	public Iterable<PostDto> findPostsByAuthor(String author) {
		return postRepository.findByAuthorIgnoreCase(author)
							 .map(post -> modelMapper.map(post, PostDto.class))
							 .collect(Collectors.toList());
	}

	@Override
	public Iterable<PostDto> findPostsByTags(List<String> tags) {
		return postRepository.findByTagsInIgnoreCase(tags)
							 .map(post -> modelMapper.map(post, PostDto.class))
							 .collect(Collectors.toList());
	}

	@Override
	public Iterable<PostDto> findPostsByPeriod(DatePeriodDto datePeriodDto) {
		LocalDateTime dateFrom = datePeriodDto.getDateFrom().atStartOfDay();
		LocalDateTime dateTo = datePeriodDto.getDateTo().atTime(23, 59, 59);
		return postRepository.findByDateCreatedBetween(dateFrom, dateTo)
							 .map(post -> modelMapper.map(post, PostDto.class))
							 .collect(Collectors.toList());
	}

}
