package com.example.tipphub.betround;

import com.example.tipphub.notification.Notification;
import com.example.tipphub.user.User;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table
public class Bet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    private String homeTeam;
    private String awayTeam;
    int homeTeamScore;
    int awayTeamScore;
    private LocalDate dateOfGame;
    private LocalDate dateOfBet;
    private int betScore;

    //@JsonIgnore
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "betround_id", referencedColumnName = "id")
    private Betround betround;

    @JsonIgnore
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private User betOwner;

    @ManyToMany(fetch = FetchType.LAZY,
            cascade = {
                    CascadeType.PERSIST,
                    CascadeType.MERGE
            },
            mappedBy = "sharedBets")
    @JsonIgnore
    private List<Notification> notifications = new ArrayList<>();

    private boolean moneyBet;
    private boolean homeTeamWinner;
    private boolean awayTeamWinner;
    private boolean draw;
    private double amountOfMoney;
    private double profit;
    private boolean evaluated;

    public Bet() {
    }

    public Bet(Long id, String homeTeam, String awayTeam,
               int homeTeamScore, int awayTeamScore, LocalDate dateOfGame,
               LocalDate dateOfBet, Betround betround, User betOwner) {
        this.id = id;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.homeTeamScore = homeTeamScore;
        this.awayTeamScore = awayTeamScore;
        this.dateOfGame = dateOfGame;
        this.dateOfBet = dateOfBet;
        this.betround = betround;
        this.betOwner = betOwner;
    }

    public Bet(String homeTeam, String awayTeam, int homeTeamScore, int awayTeamScore, LocalDate dateOfGame, LocalDate dateOfBet, int betScore, Betround betround, User betOwner, List<Notification> notifications, boolean moneyBet, boolean homeTeamWinner, boolean awayTeamWinner, boolean draw, double amountOfMoney, double profit) {
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.homeTeamScore = homeTeamScore;
        this.awayTeamScore = awayTeamScore;
        this.dateOfGame = dateOfGame;
        this.dateOfBet = dateOfBet;
        this.betScore = betScore;
        this.betround = betround;
        this.betOwner = betOwner;
        this.notifications = notifications;
        this.moneyBet = moneyBet;
        this.homeTeamWinner = homeTeamWinner;
        this.awayTeamWinner = awayTeamWinner;
        this.draw = draw;
        this.amountOfMoney = amountOfMoney;
        this.profit = profit;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getHomeTeam() {
        return homeTeam;
    }

    public void setHomeTeam(String homeTeam) {
        this.homeTeam = homeTeam;
    }

    public String getAwayTeam() {
        return awayTeam;
    }

    public void setAwayTeam(String awayTeam) {
        this.awayTeam = awayTeam;
    }

    public int getHomeTeamScore() {
        return homeTeamScore;
    }

    public void setHomeTeamScore(int homeTeamScore) {
        this.homeTeamScore = homeTeamScore;
    }

    public int getAwayTeamScore() {
        return awayTeamScore;
    }

    public void setAwayTeamScore(int awayTeamScore) {
        this.awayTeamScore = awayTeamScore;
    }

    public LocalDate getDateOfGame() {
        return dateOfGame;
    }

    public void setDateOfGame(LocalDate dateOfGame) {
        this.dateOfGame = dateOfGame;
    }

    public LocalDate getDateOfBet() {
        return dateOfBet;
    }

    public void setDateOfBet(LocalDate dateOfBet) {
        this.dateOfBet = dateOfBet;
    }

    public Betround getBetround() {
        return betround;
    }

    public void setBetround(Betround betround) {
        this.betround = betround;
    }

    public User getBetOwner() {
        return betOwner;
    }

    public void setBetOwner(User betOwner) {
        this.betOwner = betOwner;
    }

    public int getBetScore() {
        return betScore;
    }

    public void setBetScore(int betScore) {
        this.betScore = betScore;
    }

    public List<Notification> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<Notification> notifications) {
        this.notifications = notifications;
    }

    public boolean isMoneyBet() {
        return moneyBet;
    }

    public void setMoneyBet(boolean moneyBet) {
        this.moneyBet = moneyBet;
    }

    public boolean isHomeTeamWinner() {
        return homeTeamWinner;
    }

    public void setHomeTeamWinner(boolean homeTeamWinner) {
        this.homeTeamWinner = homeTeamWinner;
    }

    public boolean isAwayTeamWinner() {
        return awayTeamWinner;
    }

    public void setAwayTeamWinner(boolean awayTeamWinner) {
        this.awayTeamWinner = awayTeamWinner;
    }

    public boolean isDraw() {
        return draw;
    }

    public void setDraw(boolean draw) {
        this.draw = draw;
    }

    public double getAmountOfMoney() {
        return amountOfMoney;
    }

    public void setAmountOfMoney(double amountOfMoney) {
        this.amountOfMoney = amountOfMoney;
    }

    public double getProfit() {
        return profit;
    }

    public void setProfit(double profit) {
        this.profit = profit;
    }

    public boolean isEvaluated() {
        return evaluated;
    }

    public void setEvaluated(boolean evaluated) {
        this.evaluated = evaluated;
    }
}
