package at.alladin.rmbt.statisticServer;

public enum UsagePeriod {

    DAY("day"), WEEK("week"), MONTH("month"), YEAR("year"), COUNTRY("country"), UNKNOWN("unknown");

    private String usagePeriod;

    UsagePeriod(String usagePeriod) {
        this.usagePeriod = usagePeriod;
    }

    public String getUsagePeriod() {
        return usagePeriod;
    }

    public static UsagePeriod getValue(String period) {

        // check period
        if (period != null && period.isEmpty() == false) {

            // compare
            for (UsagePeriod usagePeriod : UsagePeriod.values()) {
                if (period.toLowerCase().compareTo(usagePeriod.getUsagePeriod()) == 0) {
                    return usagePeriod;
                }// if
            }// fort

        }// if

        return UNKNOWN;
    }

    @Override
    public String toString() {
        return usagePeriod;
    }

}
