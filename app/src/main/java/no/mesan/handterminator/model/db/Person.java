package no.mesan.handterminator.model.db;

import com.orm.SugarRecord;

import java.io.Serializable;

/**
 * @author Joakim Rishaug
 * Base-object for people that recieve or send packages
 */
public class Person extends SugarRecord<Person> implements Serializable{
    private String name, address, city, phone, zipCode;

    public Person(){

    }
    public Person(String name, String address, String city, String phone, String zipCode){
        this.name = name;
        this.address = address;
        this.city = city;
        this.phone = phone;
        this.zipCode = zipCode;
    }

    public String getPhone(){
        return phone;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getCity() {
        return city;
    }

    public String getZipCode() {
        return zipCode;
    }
}
