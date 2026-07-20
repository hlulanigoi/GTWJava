package com.goinghatway.app.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.goinghatway.app.api.responses.ApiResponse;
import com.goinghatway.app.models.Match;
import com.goinghatway.app.repositories.MatchRepository;

import java.util.List;

public class MatchViewModel extends AndroidViewModel {

    private final MatchRepository repo;

    public MatchViewModel(@NonNull Application app) {
        super(app);
        repo = new MatchRepository(app);
    }

    public MutableLiveData<ApiResponse<List<Match>>> getMyMatches() {
        return repo.getMyMatches();
    }

    public MutableLiveData<ApiResponse<Match>> acceptMatch(String matchId) {
        return repo.acceptMatch(matchId);
    }

    public MutableLiveData<ApiResponse<Match>> rejectMatch(String matchId) {
        return repo.rejectMatch(matchId);
    }

    public MutableLiveData<ApiResponse<Match>> markCollected(String matchId) {
        return repo.markCollected(matchId);
    }

    public MutableLiveData<ApiResponse<Match>> markDelivered(String matchId) {
        return repo.markDelivered(matchId);
    }
}
