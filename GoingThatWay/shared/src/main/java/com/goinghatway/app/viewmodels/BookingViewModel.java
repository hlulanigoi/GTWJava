package com.goinghatway.app.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.goinghatway.app.api.responses.ApiResponse;
import com.goinghatway.app.models.Booking;
import com.goinghatway.app.repositories.BookingRepository;

import java.util.List;

public class BookingViewModel extends AndroidViewModel {

    private final BookingRepository repo;

    public BookingViewModel(@NonNull Application app) {
        super(app);
        repo = new BookingRepository(app);
    }

    public MutableLiveData<ApiResponse<List<Booking>>> getMyBookings() {
        return repo.getMyBookings();
    }

    public MutableLiveData<ApiResponse<Booking>> acceptBooking(String bookingId) {
        return repo.acceptBooking(bookingId);
    }

    public MutableLiveData<ApiResponse<Booking>> rejectBooking(String bookingId) {
        return repo.rejectBooking(bookingId);
    }

    public MutableLiveData<ApiResponse<Booking>> markPickedUp(String bookingId) {
        return repo.markPickedUp(bookingId);
    }

    public MutableLiveData<ApiResponse<Booking>> markCompleted(String bookingId) {
        return repo.markCompleted(bookingId);
    }
}
