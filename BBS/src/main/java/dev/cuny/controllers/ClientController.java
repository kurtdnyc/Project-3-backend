package dev.cuny.controllers;

import java.util.List;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import dev.cuny.entities.Client;
import dev.cuny.exceptions.ClientAlreadyExistedException;
import dev.cuny.services.BugReportService;
import dev.cuny.services.ClientService;

@Component
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
public class ClientController {
	private static Logger logger = LoggerFactory.getLogger(ClientController.class);
	@Autowired
	ClientService cs;
	
	@Autowired
	BugReportService brs;
	
	@PostMapping(value = "/clients")
	public Client signup(@RequestBody Client client) {
		try {
			String str = "Client was created: "+ client.getcId();
			logger.info(str);
			return cs.createClient(client);
		} catch (ClientAlreadyExistedException e) {
			String str = "Unable to create the client: " + client.getcId();
			logger.info(str);
			throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE);
		}
	}

	@GetMapping(value = "/clients/login")
	public Client login(@RequestParam String username, @RequestParam String password) {
		return cs.getClientByUsernameAndPassword(username, password);
	}

	@PutMapping(value = "/clients")
	public Client updateClient(@RequestBody Client client) {
		String str = "Client was updated: " + client.getcId();
		logger.info(str);
		return cs.updateClient(client);
	}
	
	@GetMapping(value = "/clients/{id}")
	public Client getClientById(@PathVariable int id) {
			return cs.getClientById(id);
	}

	@GetMapping(value = "/clients/{id}/solutions")
	public <T> T getSolutionsByClientId(@PathVariable Integer id, @RequestParam(required = false) Boolean count) {	
		if(count == null)
			count = false;
		if(count.booleanValue()) {
			T res = (T) cs.getSolutionCountByClient(id);
			return (res != null)? res : (T) (Integer) 0;
		}
		else {
			return (T) cs.getSolutionsByClient(id);
		}
	}
	
	@GetMapping(value = "/clients/{id}/bugreports")
	public <T> T getBugReportsByClientId(@PathVariable Integer id, @RequestParam(required = false) Boolean count) {	
		if(count == null)
			count = false;
		if(count.booleanValue()) {
			try {
				Client c = cs.getClientById(id);
				Integer result = brs.getClientBugReports(c.getUsername()).size();
				return (T) result;
			} catch (NoSuchElementException e) {
				String str = "Unable to find a client with id: " + id;
				logger.error(str);
				throw new ResponseStatusException(HttpStatus.NOT_FOUND);
			}
		}
		else {
			try {
				Client c = cs.getClientById(id);
				return (T) brs.getClientBugReports(c.getUsername());
			} catch (NoSuchElementException e) {
				String str = "Unable to find a client with id: " + id;
				logger.error(str);
				throw new ResponseStatusException(HttpStatus.NOT_FOUND);
			}
		}
	}
	
	@GetMapping(value = "/clients")
	public <T> T getAllClients(
			@RequestParam (required = false) Boolean count,
			@RequestParam(required = false) String username, 
			@RequestParam(required = false) String email) {
		
		if (username != null) {
			return (T) cs.getClientByUsername(username);
		}

		if (email != null) {
			return (T) cs.getClientByEmail(email);
		}

		if(count != null) {
			Integer c = cs.getClientCount();
			return (T) c;
		}
		else {
			return (T) cs.getAllClients();
		}
	}

	@GetMapping(value = "/clients/points")
	public int getClientsPoints(@RequestParam int id) {
		return cs.getClientPoints(id);
	}
	
	@GetMapping(value = "/clients/leaderboard/username")
	public List<String> getLeaderboardusernames() {
		return cs.leaderboardusername();
	}

	@GetMapping(value = "/clients/leaderboard/points")
	public List<Integer> getLeaderboardpoints() {
		return cs.leaderboardpoints();

	}
}
