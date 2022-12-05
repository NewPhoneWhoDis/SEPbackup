package com.example.tipphub.betround;

import com.example.tipphub.email.EmailSenderService;
import com.example.tipphub.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping(path = "/betround")
public class BetroundController {

    private final BetroundService betroundService;
    private final EmailSenderService emailSenderService;

@Autowired
    public BetroundController(BetroundService betroundService, EmailSenderService emailSenderService) {
        this.betroundService = betroundService;
    this.emailSenderService = emailSenderService;
}


    @GetMapping("/all")
    public List<Betround> getAllBetrounds(){ return betroundService.getAllBetrounds();
    }


    @PutMapping("/add/{leagueId}/{ownerId}")
    public void addNewBetround(@PathVariable("leagueId") Long leagueId,
                               @PathVariable("ownerId") Long ownerId,
                               @RequestBody Betround betround){
        betroundService.addNewBetround(leagueId, ownerId, betround);
    }

    @PutMapping("/bet/{ownerId}/{betroundId}")
    public void betInRound(@PathVariable("ownerId") Long ownerId,
                           @PathVariable("betroundId") Long betroundId,
                           @RequestBody Bet bet){
        betroundService.betInRound(ownerId, betroundId, bet);
    }

    @GetMapping("/evaluation/{ownerId}/{betroundId}")
    public int getEvaluationInRound(@PathVariable("ownerId") Long ownerId,
                                    @PathVariable("betroundId") Long betroundId){

    return betroundService.getEvaluationInRound(ownerId,betroundId);
    }

    @PutMapping("/inviteGeneration/{betroundId}/{userId}/{targetetUserId}")
    public void generateInvite(@PathVariable Long betroundId, @PathVariable Long userId, @PathVariable Long targetetUserId) {
        User targetetUser = betroundService.getTargetetUser(targetetUserId);
        betroundService.generateInviteURL(betroundId, userId);
        emailSenderService.sendEmailInviteBetround(betroundId, targetetUser.getEmail());
    }

    @GetMapping("/getInivteURL")
    public String sendInviteURL(@PathVariable("betround") Betround betround) {
        return betround.getInviteURL();
    }

    @PostMapping("/onLinkClick")
    public void saveUserInBetrounds(@RequestBody User user, @RequestBody Betround betround) {
        betroundService.addInvitedUserToBetround(user, betround);
    }
}
