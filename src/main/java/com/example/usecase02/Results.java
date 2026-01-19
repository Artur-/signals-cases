package com.example.usecase02;

import com.vaadin.signals.ReferenceSignal;
import com.vaadin.signals.WritableSignal;

public class Results {
    WritableSignal<String> text = new ReferenceSignal<>("");
    WritableSignal<Boolean> visible = new ReferenceSignal<>(false);
}
