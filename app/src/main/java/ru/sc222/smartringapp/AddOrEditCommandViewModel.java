package ru.sc222.smartringapp;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AddOrEditCommandViewModel extends ViewModel {
    private MutableLiveData<Boolean> isCommandAdded; //exit activity when location is added
    private MutableLiveData<Integer> selectedIcon;

    public AddOrEditCommandViewModel() {
        isCommandAdded =new MutableLiveData<>();
        isCommandAdded.setValue(false);
        selectedIcon = new MutableLiveData<>();
        selectedIcon.setValue(0);
    }


    public void setIsCommandAdded(boolean isCommandAdded) {
        this.isCommandAdded.setValue(isCommandAdded);
    }

    public LiveData<Boolean> getIsCommandAdded() {
        return isCommandAdded;
    }

    public MutableLiveData<Integer> getSelectedIcon() {
        return selectedIcon;
    }

    public void setSelectedIcon(int background) {
        selectedIcon.setValue(background);
    }
}