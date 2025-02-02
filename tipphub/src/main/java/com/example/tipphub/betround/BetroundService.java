package com.example.tipphub.betround;

import com.example.tipphub.email.EmailSenderService;
import com.example.tipphub.hubSystem.HubSystemRepository;
import com.example.tipphub.league.*;
import com.example.tipphub.user.User;
import com.example.tipphub.user.UserRepository;
import com.example.tipphub.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BetroundService {

    private final BetroundRepository betroundRepository;
    private final BetRepository betRepository;
    private final UserRepository userRepository;
    private final LeagueRepository leagueRepository;
    private final GameRepository gameRepository;
    private final HubSystemRepository hubSystemRepository;
    private final EmailSenderService emailSenderService;
    private final BetroundNicknameRepository betroundNicknameRepository;
    //private final LeagueService leagueService;

    @Autowired
    public BetroundService(BetroundRepository betroundRepository, BetRepository betRepository,
            UserRepository userRepository,
            LeagueRepository leagueRepository,
            HubSystemRepository hubSystemRepository,
            GameRepository gameRepository,
            EmailSenderService emailSenderService,
            //LeagueService leagueService,
            BetroundNicknameRepository betroundNicknameRepository) {
        this.betroundRepository = betroundRepository;
        this.betRepository = betRepository;
        this.userRepository = userRepository;
        this.leagueRepository = leagueRepository;
        this.hubSystemRepository = hubSystemRepository;
        this.gameRepository = gameRepository;
        this.emailSenderService = emailSenderService;
        this.betroundNicknameRepository = betroundNicknameRepository;
        //this.leagueService = leagueService;
    }

    @Transactional
    public List<Betround> getAllBetrounds() {
        return betroundRepository.findAll();
    }

    @Transactional
    public void addNewBetround(Long leagueOfRound, Long ownerOfRound, Betround wantedRound) {

        League leagueOfBetround = leagueRepository.findById(leagueOfRound).get();
        wantedRound.setLeague(leagueOfBetround);

        leagueOfBetround.getBetrounds().add(wantedRound);

        User owner = userRepository.findById(ownerOfRound).get();
        wantedRound.setOwner(owner);

        owner.getBetrounds().add(wantedRound);

        betroundRepository.save(wantedRound);

        if (wantedRound.getBets() != null) {
            List<Bet> betsOfRound = wantedRound.getBets();
            for (Bet bet : betsOfRound) {
                bet.setBetround(wantedRound);
                betRepository.save(bet);
            }
        }
        leagueOfBetround.setNumberOfBetrounds(leagueOfBetround.getNumberOfBetrounds()+1);
    }

    @Transactional
    public List<Bet> getBetsOfUserInRound(Long userId, Long wantedRound) {
        List<Bet> betsOfUser = new ArrayList<>();
        for (Bet betIterator : userRepository.findById(userId).get().getBets()) {
            if (betIterator.getBetround().getId() == wantedRound) {
                betsOfUser.add(betIterator);
            }

        }
        return betsOfUser;
    }

    @Transactional
    public int getEvaluationInRound(Long userId, Long wantedRound) {
        int evaluationOfRound = 0;
        User user = userRepository.findById(userId).get();
        for (Bet betOfUser : getBetsOfUserInRound(userId, wantedRound)) {
            for (Gameday gamedayIterator : leagueRepository.findById(betOfUser.getBetround()
                    .getLeague().getId()).get().getGameSchedule().getGamedayList()) {
                for (Game actualGame : gamedayIterator.getGames()) {
                    if (betOfUser.getHomeTeam().equals(actualGame.getHomeTeam()) &&
                            betOfUser.getAwayTeam().equals(actualGame.getAwayTeam()) &&
                            betOfUser.getDateOfGame().isEqual(actualGame.getDate())) {
                        if(betOfUser.isMoneyBet() && !betOfUser.isEvaluated()){
                            if(actualGame.getScoreHomeTeam() > actualGame.getScoreAwayTeam() && betOfUser.isHomeTeamWinner()){
                                betOfUser.setProfit(actualGame.getHomeTeamOdd() * betOfUser.getAmountOfMoney());
                                user.setAccountBalance(user.getAccountBalance() + betOfUser.getProfit());
                            } else if(actualGame.getScoreAwayTeam() > actualGame.getScoreHomeTeam() && betOfUser.isAwayTeamWinner()){
                                betOfUser.setProfit(actualGame.getAwayTeamOdd() * betOfUser.getAmountOfMoney());
                                user.setAccountBalance(user.getAccountBalance() + betOfUser.getProfit());
                            } else if(actualGame.getScoreHomeTeam() - actualGame.getScoreAwayTeam() == 0 && betOfUser.isDraw()){
                                betOfUser.setProfit(actualGame.getDrawOdd() * betOfUser.getAmountOfMoney());
                                user.setAccountBalance(user.getAccountBalance() + betOfUser.getProfit());
                            }
                            betOfUser.setEvaluated(true);
                        }else{
                            if (actualGame.getScoreHomeTeam() == betOfUser.getHomeTeamScore() &&
                                    actualGame.getScoreAwayTeam() == betOfUser.getAwayTeamScore()) {
                                betOfUser.setBetScore(betOfUser.getBetround().getScoreRightResult());
                                evaluationOfRound += betOfUser.getBetScore();
                            } else if (((actualGame.getScoreHomeTeam() - actualGame.getScoreAwayTeam())
                                    * -1) == ((betOfUser.getHomeTeamScore() - betOfUser.getAwayTeamScore()) * -1)) {
                                betOfUser.setBetScore(betOfUser.getBetround().getScoreRightDiff());
                                evaluationOfRound += betOfUser.getBetScore();
                            } else if ((actualGame.getScoreHomeTeam() > actualGame.getScoreAwayTeam() &&
                                    betOfUser.getHomeTeamScore() > betOfUser.getAwayTeamScore()) ||
                                    (actualGame.getScoreHomeTeam() < actualGame.getScoreAwayTeam() &&
                                            betOfUser.getHomeTeamScore() < betOfUser.getAwayTeamScore())) {
                                betOfUser.setBetScore(betOfUser.getBetround().getScoreRightWin());
                                evaluationOfRound += betOfUser.getBetScore();
                            } else {
                                betOfUser.setBetScore(0);
                                evaluationOfRound += betOfUser.getBetScore();
                            }
                        }
                    }
                }
            }
        }
        return evaluationOfRound;
    }

    @Transactional
    public List<Bet> getPossibleBetsInRound(Betround betround, LocalDate dateOfDay) {
        List<Gameday> remainingGames = betround.getLeague().getGameSchedule().getGamedayList();
        List<Bet> possibleBets = new ArrayList<>();
        for (Gameday gameday : remainingGames) {
            for (Game gameIterator : gameday.getGames()) {
                if (dateOfDay.isAfter(gameIterator.getDate()) ||
                        dateOfDay.isEqual(gameIterator.getDate())) {
                    Bet gameToBet = new Bet(gameIterator.getId(), gameIterator.getHomeTeam(),
                            gameIterator.getAwayTeam(), 0,
                            0, gameIterator.getDate(), dateOfDay, betround, null);
                    betRepository.save(gameToBet);
                    possibleBets.add(gameToBet);
                }
            }
        }
        return possibleBets;
    }

    @Transactional
    public void betInRound(Long ownerId, Long betroundId, Bet wantedBet) throws RuntimeException {

        wantedBet.setBetOwner(userRepository.findById(ownerId).get());
        wantedBet.setBetround(betroundRepository.findById(betroundId).get());

        Game game = getGameForBet(wantedBet);
        if (game != null) {

            User owner = userRepository.findById(ownerId).get();
            if(wantedBet.isMoneyBet()){
                if(owner.getAccountBalance() < wantedBet.getAmountOfMoney()){
                    throw new RuntimeException();
                }
                owner.setAccountBalance(owner.getAccountBalance() - wantedBet.getAmountOfMoney());
            }

            betRepository.save(wantedBet);
            Betround wantedRound = betroundRepository.findById(betroundId).get();

            owner.getBets().add(wantedBet);
            if (!(owner.getBetrounds().contains(wantedRound))) {
                owner.getBetrounds().add(wantedRound);
            }

            wantedRound.getBets().add(wantedBet);
            wantedRound.getUsers().add(owner);

            League league = wantedRound.getLeague();
            league.setNumberOfBettors(league.getNumberOfBettors()+1);
        }

    }

    @Transactional
    public Game getBetHelp(Long gameId) {
        Game game = gameRepository.findById(gameId).get();
        String homeTeam = game.getHomeTeam();
        String awayTeam = game.getAwayTeam();
        int scoreHomeTeam = 0;
        int scoreAwayTeam = 0;
        List<Gameday> gamedayList = game.getGameday().getGameSchedule().getGamedayList();
        int counterHomeTeam = 0;
        int counterAwayTeam = 0;
        for (Gameday gameday : gamedayList) {
            for (Game currentGame : gameday.getGames()) {
                if (currentGame.getDate().isBefore(game.getDate())) {
                    if (currentGame.getHomeTeam().equals(homeTeam)) {
                        scoreHomeTeam += currentGame.getScoreHomeTeam();
                        counterHomeTeam += 1;
                    }
                    if (currentGame.getAwayTeam().equals(awayTeam)) {
                        scoreAwayTeam += currentGame.getScoreAwayTeam();
                        counterAwayTeam += 1;
                    }
                }
            }
        }
        Game returnGame = new Game();
        if (counterHomeTeam != 0) {
            int resultHomeTeam = (int) Math.round((double) scoreHomeTeam / (double) counterHomeTeam);
            returnGame.setScoreHomeTeam(resultHomeTeam);
        }
        if (counterAwayTeam != 0) {
            int resultAwayTeam = (int) Math.round((double) scoreAwayTeam / (double) counterAwayTeam);
            returnGame.setScoreAwayTeam(resultAwayTeam);
        }
        returnGame.setAwayTeam(awayTeam);
        returnGame.setHomeTeam(homeTeam);
        return returnGame;
    }

    public String generateInviteURL(Long betroundId, Long userId) {
        Betround betround = betroundRepository.findById(betroundId).get();
        User user = userRepository.findById(userId).get();

        String generatedURL = betround.getName() + betround.getId();
        betround.setInviteURL(generatedURL);
        /*
         * List<User> extendedList = betround.getUsers();
         * extendedList.add(user);
         * betround.setUsers(extendedList);
         */

        betround.getUsers().add(user);

        betroundRepository.save(betround);
        return generatedURL;
    }

    @Transactional
    public List<String> getTopThreeTeams(Long leagueId) {
        List<String> returnList = new ArrayList<>();
        League league = leagueRepository.findById(leagueId).get();
        List<Betround> betrounds = league.getBetrounds();
        Hashtable<String, Integer> teamsWithScore = new Hashtable<String, Integer>();
        List<String> teamsList = getAllTeams(leagueId);
        for (String team : teamsList) {
            teamsWithScore.put(team, 0);
        }
        for (Betround betround : betrounds) {
            for (Bet bet : betround.getBets()) {
                Game game = getGameForBet(bet);
                teamsWithScore.put(bet.getHomeTeam(),
                        teamsWithScore.get(bet.getHomeTeam()) + bet.getBetScore() / 2);
                teamsWithScore.put(bet.getAwayTeam(),
                        teamsWithScore.get(bet.getAwayTeam()) + bet.getBetScore() / 2);
            }
        }
        String team1 = getTeamWithMaxScore(teamsWithScore);
        teamsWithScore.remove(team1);
        returnList.add(team1);
        String team2 = getTeamWithMaxScore(teamsWithScore);
        teamsWithScore.remove(team2);
        returnList.add(team2);
        String team3 = getTeamWithMaxScore(teamsWithScore);
        returnList.add(team3);

        return returnList;
    }

    @Transactional
    public void saveUserInBetrounds(Long betroundId, Long userId) {
        User user = userRepository.findById(userId).get();
        Betround betround = betroundRepository.findById(betroundId).get();
        if (betround.getUsers().contains(user)) {
            return;
        }
        user.getBetrounds().add(betround);
        betround.getUsers().add(user);
    }

    public void sendEmailBetroundInvite(Long betroundId, Long userId) {
        User user = userRepository.findById(userId).get();
        this.emailSenderService.sendEmailInviteBetround(betroundId, user.getEmail(), userId);
    }

    public User getUserById(Long userId) {
        System.out.println("This is the id of the user:" + userId);
        return userRepository.findById(userId).get();
    }

    public Betround getBetroundById(Long betId) {
        return betroundRepository.findById(betId).get();
    }

    @Transactional
    public Game getGameForBet(Bet bet) {
        for (Gameday gameday : bet.getBetround().getLeague().getGameSchedule().getGamedayList()) {
            for (Game game : gameday.getGames()) {
                if (game.getAwayTeam().equals(bet.getAwayTeam()) &&
                        game.getHomeTeam().equals(bet.getHomeTeam()) &&
                        game.getDate().isEqual(bet.getDateOfGame())) {
                    return game;
                }
            }
        }
        return null;
    }

    @Transactional
    public List<String> getAllTeams(Long leagueId) {
        List<String> returnList = new ArrayList<>();
        League league = leagueRepository.findById(leagueId).get();
        for (Gameday gameday : league.getGameSchedule().getGamedayList()) {
            for (Game game : gameday.getGames()) {
                if (!returnList.contains(game.getHomeTeam())) {
                    returnList.add(game.getHomeTeam());
                }
                if (!returnList.contains(game.getAwayTeam())) {
                    returnList.add(game.getAwayTeam());
                }
            }
        }
        return returnList;
    }

    public String getTeamWithMaxScore(Hashtable<String, Integer> teamsWithScore) {
        Set<String> teams = teamsWithScore.keySet();
        String returnTeam = "";
        int highestScore = 0;
        for (String key : teams) {
            if (teamsWithScore.get(key) >= highestScore) {
                returnTeam = key;
                highestScore = teamsWithScore.get(key);
            }
        }
        if (teamsWithScore.get(returnTeam) > 0) {
            return returnTeam;
        } else {
            return "";
        }

    }

    @Transactional
    public int getTotalScoreOfUserForLeague(Long leagueId, Long userId) {
        League league = leagueRepository.findById(leagueId).get();
        int totalScoreOfUserInLeague = 0;
        for (Betround betroundIterator : league.getBetrounds()) {
            for (User userIterator : betroundIterator.getUsers()) {
                if (userIterator.getId() == userId) {
                    totalScoreOfUserInLeague += getEvaluationInRound(userIterator.getId(), betroundIterator.getId());
                }
            }
        }
        return totalScoreOfUserInLeague;
    }

    @Transactional
    public List<String> getBestUsersOfLeague(Long leagueId) {

        Hashtable<String, Integer> userWithScore = new Hashtable<>();
        League league = leagueRepository.findById(leagueId).get();
        for (Betround betroundIterator : league.getBetrounds()) {
            for (User userIterator : betroundIterator.getUsers()) {
                userWithScore.put(userIterator.getEmail(),
                        getTotalScoreOfUserForLeague(leagueId, userIterator.getId()));
            }
        }
        List<String> top3 = userWithScore.entrySet().stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .limit(3)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        for (int i = 0; i < top3.size(); i++) {
            User user = userRepository.findByEmail(top3.get(i)).get();
            top3.set(i, user.getFirstName() + " " + user.getLastName());
        }

        System.out.println(top3);
        return top3;
    }


    /*
    @Transactional
    public void sendBetroundInviteToUser(Long betroundId, String userEmail) {
        this.emailSenderService.sendEmailInviteBetround(betroundId, userEmail);
    }
    */

    @Transactional
    public List<User> getAllParticipantsService(Long id) {
        return betroundRepository.findById(id).get().getUsers();
    }

    @Transactional
    public void setNickname(Long userId, Long betroundId, String nickname) {
        User user = userRepository.findById(userId).get();
        Betround betround = betroundRepository.findById(betroundId).get();

        for(BetroundNickname betroundNickname: betroundNicknameRepository.findAll()){
            if(betroundNickname.getUser().equals(user) && betroundNickname.getBetround().equals(betround)){
                betroundNickname.setNickname(nickname);
                return;
            }
        }

        BetroundNickname betroundNickname = new BetroundNickname();
        betroundNickname.setNickname(nickname);
        betroundNickname.setUser(user);
        betroundNickname.setBetround(betround);
        betroundNicknameRepository.save(betroundNickname);
    }

    @Transactional
    public String getNickname(Long userId, Long betroundId) {
        List<BetroundNickname> betroundNicknames = betroundNicknameRepository.findAll();
        for (BetroundNickname nickname : betroundNicknames) {
            if (Objects.equals(nickname.getUser().getId(),userId) && Objects.equals(nickname.getBetround().getId(),betroundId))
                return nickname.getNickname();
        }
        return null;
    }

    public Long getLeagueId(Long betroundId) {
        Betround betround = betroundRepository.findById(betroundId).get();
        return betround.getLeague().getId();
    }

    @Transactional
    public int countBetAmountOfUserInRound(Long userId, Long betroundId) {
        int count = 0;
        User wantedUser = userRepository.findById(userId).get();
        if (wantedUser.getBets().isEmpty()) {
            return 0;
        }
        for (Bet betIterator : wantedUser.getBets()) {
            if(betIterator.getBetround().getId()==betroundId){
                count++;
            }
        }
        return count;


    }

    @Transactional
    public Set<Map.Entry<String, Integer>> getBetAmountPerUserInRound(Long betroundId) {
        Hashtable<String, Integer> userAndBets = new Hashtable<>();
        Betround wantedRound = betroundRepository.findById(betroundId).get();
        for (Bet betIterator : wantedRound.getBets()) {
            if (!(userAndBets.containsKey(betIterator.getBetOwner()))) {
                userAndBets.put(betIterator.getBetOwner().getFirstName() +" "+ betIterator.getBetOwner().getLastName()
                        , countBetAmountOfUserInRound(betIterator.getBetOwner().getId(), betroundId));
            }

        }
        return userAndBets.entrySet();
    }

    public List<String> returnBettorsForBarDiagram(Long betroundId){
        List<String> keys = getBetAmountPerUserInRound(betroundId).stream()
                .map(entry -> entry.getKey())
                .collect(Collectors.toList());
        return keys;
    }

    public List<Integer> returnAmountBetsForBarDiagram(Long betroundId){
        List<Integer> values = getBetAmountPerUserInRound(betroundId).stream()
                .map(entry -> entry.getValue())
                .collect(Collectors.toList());
        return values;
    }






    @Transactional
    public int getEvaluationOfASingleTeamForAUserInRound(long userId, String teamName){
        int evaluationOfTeam = 0;
       for(Bet betIterator: userRepository.findById(userId).get().getBets()) {
           if((betIterator.getHomeTeam().equalsIgnoreCase(teamName))||(betIterator.getAwayTeam().equalsIgnoreCase(teamName)))
           for (Gameday gamedayIterator : leagueRepository.findById(betIterator.getBetround()
                   .getLeague().getId()).get().getGameSchedule().getGamedayList()) {
               for (Game actualGame : gamedayIterator.getGames()) {
                   if (betIterator.getHomeTeam().equals(actualGame.getHomeTeam()) &&
                           betIterator.getAwayTeam().equals(actualGame.getAwayTeam()) &&
                            betIterator.getDateOfGame().isEqual(actualGame.getDate())) {

                       if (actualGame.getScoreHomeTeam() == betIterator.getHomeTeamScore() &&
                               actualGame.getScoreAwayTeam() ==  betIterator.getAwayTeamScore()) {
                            betIterator.setBetScore(betIterator.getBetround().getScoreRightResult());
                           evaluationOfTeam += betIterator.getBetScore();
                       } else if (((actualGame.getScoreHomeTeam() - actualGame.getScoreAwayTeam())
                               * -1) == ((betIterator.getHomeTeamScore() - betIterator.getAwayTeamScore()) * -1)) {
                           betIterator.setBetScore(betIterator.getBetround().getScoreRightDiff());
                           evaluationOfTeam += betIterator.getBetScore();
                       } else if ((actualGame.getScoreHomeTeam() > actualGame.getScoreAwayTeam() &&
                               betIterator.getHomeTeamScore() > betIterator.getAwayTeamScore()) ||
                               (actualGame.getScoreHomeTeam() < actualGame.getScoreAwayTeam() &&
                                       betIterator.getHomeTeamScore() < betIterator.getAwayTeamScore())) {
                           betIterator.setBetScore(betIterator.getBetround().getScoreRightWin());
                           evaluationOfTeam += betIterator.getBetScore();
                       } else {
                           betIterator.setBetScore(0);
                           evaluationOfTeam += betIterator.getBetScore();
                       }

                   }

               }
           }
       }
        return evaluationOfTeam;
        }



        @Transactional
        public Set<Map.Entry<String, Integer>> getPointsAUserMadeFromATeam (long userId, long betroundId) {
        User wantedUser= userRepository.findById(userId).get();
        List<Bet> betsOfRound= new ArrayList<>();
        for(Bet betIterator: wantedUser.getBets()){
            if(betIterator.getBetround().getId()==betroundId){
                betsOfRound.add(betIterator);
            }
        }
        Hashtable<String, Integer> teamWithScore = new Hashtable<>();
        for(Bet betInRoundIterator: betsOfRound){
            teamWithScore.put(betInRoundIterator.getHomeTeam(),getEvaluationOfASingleTeamForAUserInRound(userId,betInRoundIterator.getHomeTeam()));
            teamWithScore.put(betInRoundIterator.getAwayTeam(),getEvaluationOfASingleTeamForAUserInRound(userId,betInRoundIterator.getAwayTeam()));

        }
        return teamWithScore.entrySet();
        }

    public List<String> returnTeamsForPieDiagram(Long userId,Long betroundId){
        List<String> keys = getPointsAUserMadeFromATeam(userId, betroundId).stream()
                .map(entry -> entry.getKey())
                .collect(Collectors.toList());
        return keys;
    }

    public List<Integer> returnPointsForPieDiagram(Long userId,Long betroundId){
        List<Integer> values = getPointsAUserMadeFromATeam(userId, betroundId).stream()
                .map(entry -> entry.getValue())
                .collect(Collectors.toList());
        return values;
    }



}
