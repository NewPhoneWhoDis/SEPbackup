package com.example.tipphub.league;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String homeTeam;
    private String awayTeam;
    private int scoreHomeTeam;
    private int scoreAwayTeam;
    private LocalDate date;
    @JsonIgnore
    @ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    @JoinColumn(name = "gameday_id", referencedColumnName = "id")
    private Gameday gameday;

    private double homeTeamOdd;
    private double awayTeamOdd;
    private double drawOdd;

    public Game() {
    }

    public Game(String homeTeam, String awayTeam, int scoreHomeTeam, int scoreAwayTeam, LocalDate date, Gameday gameday, double homeTeamOdd, double awayTeamOdd, double drawOdd) {
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.scoreHomeTeam = scoreHomeTeam;
        this.scoreAwayTeam = scoreAwayTeam;
        this.date = date;
        this.gameday = gameday;
        this.homeTeamOdd = homeTeamOdd;
        this.awayTeamOdd = awayTeamOdd;
        this.drawOdd = drawOdd;
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

    public Gameday getGameday() {
        return gameday;
    }

    public void setGameday(Gameday gameday) {
        this.gameday = gameday;
    }

    public int getScoreHomeTeam() {
        return scoreHomeTeam;
    }

    public void setScoreHomeTeam(int scoreHomeTeam) {
        this.scoreHomeTeam = scoreHomeTeam;
    }

    public int getScoreAwayTeam() {
        return scoreAwayTeam;
    }

    public void setScoreAwayTeam(int scoreAwayTeam) {
        this.scoreAwayTeam = scoreAwayTeam;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public double getHomeTeamOdd() {
        return homeTeamOdd;
    }

    public void setHomeTeamOdd(double homeTeamOdd) {
        this.homeTeamOdd = homeTeamOdd;
    }

    public double getAwayTeamOdd() {
        return awayTeamOdd;
    }

    public void setAwayTeamOdd(double awayTeamOdd) {
        this.awayTeamOdd = awayTeamOdd;
    }

    public double getDrawOdd() {
        return drawOdd;
    }

    public void setDrawOdd(double drawOdd) {
        this.drawOdd = drawOdd;
    }
}

