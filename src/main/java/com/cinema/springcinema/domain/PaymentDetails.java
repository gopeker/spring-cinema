package com.cinema.springcinema.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class PaymentDetails {
    @Column(name = "card_holder_name")
    private String cardHolderName;
    @Column(name = "card_last_four")
    private String cardLastFour;
    @Column(name = "card_expiry")
    private String cardExpiry;

    public PaymentDetails() {}

    public String getCardHolderName() { return cardHolderName; }
    public void setCardHolderName(String cardHolderName) { this.cardHolderName = cardHolderName; }
    public String getCardLastFour() { return cardLastFour; }
    public void setCardLastFour(String cardLastFour) { this.cardLastFour = cardLastFour; }
    public String getCardExpiry() { return cardExpiry; }
    public void setCardExpiry(String cardExpiry) { this.cardExpiry = cardExpiry; }
}
