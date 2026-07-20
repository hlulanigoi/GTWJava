package com.goinghatway.app.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.goinghatway.app.api.responses.ApiResponse;
import com.goinghatway.app.models.Ticket;
import com.goinghatway.app.repositories.TicketRepository;

import java.util.List;
import java.util.Map;

public class TicketViewModel extends AndroidViewModel {

    private final TicketRepository repo;

    public TicketViewModel(@NonNull Application app) {
        super(app);
        repo = new TicketRepository(app);
    }

    public MutableLiveData<ApiResponse<List<Ticket>>> getMyTickets() {
        return repo.getMyTickets();
    }

    public MutableLiveData<ApiResponse<Map<String, Double>>> getTicketPrice() {
        return repo.getTicketPrice();
    }

    public MutableLiveData<ApiResponse<Ticket>> purchaseTicket(String paymentReference) {
        return repo.purchaseTicket(paymentReference);
    }
}
