package com.mcool.sai.legal;

public interface LegalStuffProvider {

    boolean hasPrivacyPolicy();

    String getPrivacyPolicyUrl();

    boolean hasEula();

    String getEulaUrl();

}
