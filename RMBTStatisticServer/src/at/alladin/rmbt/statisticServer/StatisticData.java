package at.alladin.rmbt.statisticServer;

import java.sql.Date;

/**
 * @author Tomas Hreben
 * @data 25.september 2017
 */
public class StatisticData implements Comparable{
    private Date dayFrom;
    private long countTests;
    private long countClients;
    private long countIPs;
    private String country;

    public StatisticData(long countTests, long countClients, long countIPs, Date dayFrom, String country) {
        this.countTests = countTests;
        this.countClients = countClients;
        this.countIPs = countIPs;
        this.dayFrom = dayFrom;
        this.country = country;
    }

    public StatisticData() {
        this.countClients = 0L;
        this.countIPs = 0L;
        this.countTests = 0L;
        this.country = null;
    }

    public long getCountTests() {
        return countTests;
    }

    public void setCountTests(long countTests) {
        this.countTests = countTests;
    }

    public long getCountClients() {
        return countClients;
    }

    public void setCountClients(long countClients) {
        this.countClients = countClients;
    }

    public long getCountIPs() {
        return countIPs;
    }

    public void setCountIPs(long countIPs) {
        this.countIPs = countIPs;
    }

    public Date getDayFrom() {
        return dayFrom;
    }

    public void setDayFrom(Date dayFrom) {
        this.dayFrom = dayFrom;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void addCountIPs(long countIPs) {
        this.countIPs += countIPs;
    }

    public void addCountClients(long countClients) {
        this.countClients += countClients;
    }

    public void addCountTests(long countTests) {
        this.countTests += countTests;
    }

    public boolean sameDate(Date dayFrom) {
        return (this.dayFrom.getTime() == dayFrom.getTime() ? true : false);
    }

    @Override
    public int compareTo(Object o) {
        if (this.getCountTests() == ((StatisticData)o).getCountTests()){
            return 0;
        } else if (this.getCountTests() < ((StatisticData)o).getCountTests()){
            return 1;
        } else {
            return -1;
        }
    }
}
