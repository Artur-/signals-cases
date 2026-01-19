package com.example.usecase02;

// Bean to hold all form data
public class VisaApplicationData {
    private Boolean needsVisa = false;
    private String currentVisaStatus = "";
    private VisaType visaType = VisaType.H1B;

    // H1B fields
    private Boolean hasH1BPreviously = false;
    private String h1bSpecialtyOccupation = "";
    private String previousEmployer = "";
    private String previousPetitionNumber = "";
    private String previousH1BStartDate = "";

    // L1 fields
    private String parentCompanyName = "";
    private String yearsWithParentCompany = "";
    private String l1Category = "";

    // O1 fields
    private String fieldOfExtraordinaryAbility = "";
    private String majorAwards = "";
    private String publications = "";

    // Getters and setters
    public Boolean getNeedsVisa() {
        return needsVisa;
    }

    public void setNeedsVisa(Boolean needsVisa) {
        this.needsVisa = needsVisa;
    }

    public String getCurrentVisaStatus() {
        return currentVisaStatus;
    }

    public void setCurrentVisaStatus(String currentVisaStatus) {
        this.currentVisaStatus = currentVisaStatus;
    }

    public VisaType getVisaType() {
        return visaType;
    }

    public void setVisaType(VisaType visaType) {
        this.visaType = visaType;
    }

    public Boolean getHasH1BPreviously() {
        return hasH1BPreviously;
    }

    public void setHasH1BPreviously(Boolean hasH1BPreviously) {
        this.hasH1BPreviously = hasH1BPreviously;
    }

    public String getH1bSpecialtyOccupation() {
        return h1bSpecialtyOccupation;
    }

    public void setH1bSpecialtyOccupation(String h1bSpecialtyOccupation) {
        this.h1bSpecialtyOccupation = h1bSpecialtyOccupation;
    }

    public String getPreviousEmployer() {
        return previousEmployer;
    }

    public void setPreviousEmployer(String previousEmployer) {
        this.previousEmployer = previousEmployer;
    }

    public String getPreviousPetitionNumber() {
        return previousPetitionNumber;
    }

    public void setPreviousPetitionNumber(String previousPetitionNumber) {
        this.previousPetitionNumber = previousPetitionNumber;
    }

    public String getPreviousH1BStartDate() {
        return previousH1BStartDate;
    }

    public void setPreviousH1BStartDate(String previousH1BStartDate) {
        this.previousH1BStartDate = previousH1BStartDate;
    }

    public String getParentCompanyName() {
        return parentCompanyName;
    }

    public void setParentCompanyName(String parentCompanyName) {
        this.parentCompanyName = parentCompanyName;
    }

    public String getYearsWithParentCompany() {
        return yearsWithParentCompany;
    }

    public void setYearsWithParentCompany(String yearsWithParentCompany) {
        this.yearsWithParentCompany = yearsWithParentCompany;
    }

    public String getL1Category() {
        return l1Category;
    }

    public void setL1Category(String l1Category) {
        this.l1Category = l1Category;
    }

    public String getFieldOfExtraordinaryAbility() {
        return fieldOfExtraordinaryAbility;
    }

    public void setFieldOfExtraordinaryAbility(
            String fieldOfExtraordinaryAbility) {
        this.fieldOfExtraordinaryAbility = fieldOfExtraordinaryAbility;
    }

    public String getMajorAwards() {
        return majorAwards;
    }

    public void setMajorAwards(String majorAwards) {
        this.majorAwards = majorAwards;
    }

    public String getPublications() {
        return publications;
    }

    public void setPublications(String publications) {
        this.publications = publications;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Visa Application Data:\n");
        sb.append("--------------------\n");
        sb.append("Needs Visa: ").append(needsVisa).append("\n");

        if (needsVisa) {
            sb.append("Current Visa Status: ").append(currentVisaStatus)
                    .append("\n");
            sb.append("Visa Type: ").append(visaType).append("\n\n");

            if (visaType == VisaType.H1B) {
                sb.append("H1-B Details:\n");
                sb.append("  Specialty Occupation: ")
                        .append(h1bSpecialtyOccupation).append("\n");
                sb.append("  Had H1-B Previously: ")
                        .append(hasH1BPreviously).append("\n");
                if (hasH1BPreviously) {
                    sb.append("  Previous Employer: ")
                            .append(previousEmployer).append("\n");
                    sb.append("  Previous Petition #: ")
                            .append(previousPetitionNumber).append("\n");
                    sb.append("  Previous Start Date: ")
                            .append(previousH1BStartDate).append("\n");
                }
            } else if (visaType == VisaType.L1) {
                sb.append("L1 Details:\n");
                sb.append("  Parent Company: ").append(parentCompanyName)
                        .append("\n");
                sb.append("  Years with Company: ")
                        .append(yearsWithParentCompany).append("\n");
                sb.append("  L1 Category: ").append(l1Category)
                        .append("\n");
            } else if (visaType == VisaType.O1) {
                sb.append("O1 Details:\n");
                sb.append("  Field of Extraordinary Ability: ")
                        .append(fieldOfExtraordinaryAbility).append("\n");
                sb.append("  Major Awards: ").append(majorAwards)
                        .append("\n");
                sb.append("  Publications: ").append(publications)
                        .append("\n");
            }
        }

        return sb.toString();
    }
}
