package ru.sc222.smartringapp.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import ru.sc222.smartringapp.db.Action;

public class CommandsViewModel extends ViewModel {

    private MutableLiveData<String> mText;
    private MutableLiveData<List<Action>> actions;

    public CommandsViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is commands fragment");

        actions = new MutableLiveData<>();
        actions.setValue(new ArrayList<>());
    }

    public LiveData<String> getText() {
        return mText;
    }

    public LiveData<List<Action>> getActions() {
        return actions;
    }

    public void setActions(List<Action> actions) {
        this.actions.setValue(actions);
    }
}