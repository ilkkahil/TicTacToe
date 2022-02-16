package homework.data;

import java.net.URI;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

// Last time I worked on REST services was a few years ago.
// Back then I generated ready endpoints from a WSDL.
// This is the first time I've worked with Spring framework/Spring boot
// and swagger yaml...
// So this may be a bit of a mess

// There are some mistakes in the parameters and return values.
// Some of the features seem to work however.

@RestController
public class GameController {
	
	@Autowired
	private final GameServiceImpl game_container;
	
	GameController(GameServiceImpl container)
	{
		this.game_container = container;
	}
	
	
	// Get request for all the games
	@GetMapping("/api/v1/games")
	public ResponseEntity<Game[]> get_all_games()
	{
		return new ResponseEntity<>(game_container.getAllGames(), HttpStatus.OK);		
	}
	
	// Get request for a specific game
	@GetMapping("/api/v1/games/{id}")
	public ResponseEntity<Game> get_game_by_id(@PathVariable String id)
	{
		// Validate the required id
		if(id == null || id.length() == 0)
		{
			return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
		}
		
		Game found_game = game_container.getGame(id);
		
		if(found_game == null) {
			return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
		}
		
		return new ResponseEntity<>(found_game, HttpStatus.OK);
	}
	
	// Delete a specific game
	@DeleteMapping("/api/v1/games/{id}")
	public ResponseEntity<Object> delete_game(@PathVariable String id)
	{
		
		if(id == null || id.length() == 0)
		{
			return new ResponseEntity<Object>(HttpStatus.BAD_REQUEST);
		}
		
		if(game_container.getGame(id) == null)
		{
			return new ResponseEntity<Object>(HttpStatus.NOT_FOUND);
		}
		
		game_container.deleteGame(id);	
		
		
		return new ResponseEntity<Object>(HttpStatus.OK);
	}
	
	// Post for a new game
	@PostMapping("/api/v1/games")
	public ResponseEntity<Object> start_game(@RequestBody Game new_game)
	{
		if(new_game == null)
		{
			return new ResponseEntity<Object>("reason: Missing request-body.", HttpStatus.BAD_REQUEST);
		}
		
		if(new_game.getBoard().length() != 9)
		{
			return new ResponseEntity<Object>("reason: Game board must be 3x3", HttpStatus.BAD_REQUEST);
		}
				
		Game actual_game = game_container.startGame(new_game);
		
		// Add location to the response header		
		URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(actual_game.getId()).toUri();
		HttpHeaders response_header = new HttpHeaders();
		response_header.set("Location", location.toString());
		
		return ResponseEntity.created(location).headers(response_header).body("Location: " + location.toString());
	}
	
	// Put for a move in an existing game
	@PutMapping("/api/v1/games/{id}")
	public ResponseEntity<Object> make_move(@PathVariable String id, @RequestBody Game new_game)
	{
		if(id == null || id.length() == 0)
		{
			return new ResponseEntity<Object>("Missing request id.", HttpStatus.BAD_REQUEST);
		}
		
		if(new_game == null)
		{
			return new ResponseEntity<Object>("Missing request body.", HttpStatus.BAD_REQUEST);
		}
		
		if(game_container.getGame(id) == null)
		{
			return new ResponseEntity<Object>(null, HttpStatus.NOT_FOUND);
		}
		
		HashMap<Boolean, String> move_response = game_container.makeAMove(id, new_game.getBoard()); 
		
		if(move_response.containsKey(Boolean.FALSE))
		{
			return new ResponseEntity<Object>(move_response.get(Boolean.FALSE), HttpStatus.BAD_REQUEST);			
		}
		
		return new ResponseEntity<Object>(game_container.getGame(id), HttpStatus.OK);
	}
}
