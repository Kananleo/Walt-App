package com.walt;

import com.walt.Exceptions.NoAvailableDriversException;
import com.walt.Exceptions.NoCustomerException;
import com.walt.Exceptions.NotSameCityException;
import com.walt.dao.*;
import com.walt.model.City;
import com.walt.model.Customer;
import com.walt.model.Driver;
import com.walt.model.Restaurant;
import com.walt.model.Delivery;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.annotation.DirtiesContext;

import javax.annotation.Resource;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@SpringBootTest()
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class WaltTest {

    @TestConfiguration
    static class WaltServiceImplTestContextConfiguration {

        @Bean
        public WaltService waltService() {
            return new WaltServiceImpl();
        }
    }

    @Autowired
    WaltService waltService;

    @Resource
    CityRepository cityRepository;

    @Resource
    CustomerRepository customerRepository;

    @Resource
    DriverRepository driverRepository;

    @Resource
    DeliveryRepository deliveryRepository;

    @Resource
    RestaurantRepository restaurantRepository;

    @BeforeEach()
    public void prepareData(){

        City jerusalem = new City("Jerusalem");
        City tlv = new City("Tel-Aviv");
        City bash = new City("Beer-Sheva");
        City haifa = new City("Haifa");

        cityRepository.save(jerusalem);
        cityRepository.save(tlv);
        cityRepository.save(bash);
        cityRepository.save(haifa);

        createDrivers(jerusalem, tlv, bash, haifa);

        createCustomers(jerusalem, tlv, haifa);

        createRestaurant(jerusalem, tlv);
    }

    private void createRestaurant(City jerusalem, City tlv) {
        Restaurant meat = new Restaurant("meat", jerusalem, "All meat restaurant");
        Restaurant vegan = new Restaurant("vegan", tlv, "Only vegan");
        Restaurant cafe = new Restaurant("cafe", tlv, "Coffee shop");
        Restaurant chinese = new Restaurant("chinese", tlv, "chinese restaurant");
        Restaurant mexican = new Restaurant("mexican", tlv, "mexican restaurant ");

        restaurantRepository.saveAll(Lists.newArrayList(meat, vegan, cafe, chinese, mexican));
    }

    private void createCustomers(City jerusalem, City tlv, City haifa) {
        Customer beethoven = new Customer("Beethoven", tlv, "Ludwig van Beethoven");
        Customer mozart = new Customer("Mozart", jerusalem, "Wolfgang Amadeus Mozart");
        Customer chopin = new Customer("Chopin", haifa, "Frédéric François Chopin");
        Customer rachmaninoff = new Customer("Rachmaninoff", tlv, "Sergei Rachmaninoff");
        Customer bach = new Customer("Bach", tlv, "Sebastian Bach. Johann");

        customerRepository.saveAll(Lists.newArrayList(beethoven, mozart, chopin, rachmaninoff, bach));
    }

    private void createDrivers(City jerusalem, City tlv, City bash, City haifa) {
        Driver mary = new Driver("Mary", tlv);
        Driver patricia = new Driver("Patricia", tlv);
        Driver jennifer = new Driver("Jennifer", haifa);
        Driver james = new Driver("James", bash);
        Driver john = new Driver("John", bash);
        Driver robert = new Driver("Robert", jerusalem);
        Driver david = new Driver("David", jerusalem);
        Driver daniel = new Driver("Daniel", tlv);
        Driver noa = new Driver("Noa", haifa);
        Driver ofri = new Driver("Ofri", haifa);
        Driver nata = new Driver("Neta", jerusalem);

        driverRepository.saveAll(Lists.newArrayList(mary, patricia, jennifer, james, john, robert, david, daniel, noa, ofri, nata));
    }

    @Test
    public void testBasics(){
        assertEquals(((List<City>) cityRepository.findAll()).size(),4);
        assertEquals((driverRepository.findAllDriversByCity(cityRepository.findByName("Beer-Sheva")).size()), 2);
    }

    @Test
    public void testCreateOrderAndFindDriver() throws NotSameCityException, NoAvailableDriversException, NoCustomerException {
        Customer customer = customerRepository.findByName("Bach");
        Restaurant restaurant = restaurantRepository.findByName("cafe");
        Date date = new Date();
        Delivery delivery = waltService.createOrderAndAssignDriver(customer, restaurant, date);
    	assertNotNull(delivery);
    	assertEquals(customer.getCity().getName(), delivery.getDriver().getCity().getName());
    }

    @Test
    // In this test we know we wont get an NonAvailableDriversException\NoCustomerException!
    public void testNotTheSameCity() {
        Customer customer = customerRepository.findByName("Bach");
        Restaurant restaurant = restaurantRepository.findByName("meat");
        Date deliveryTime = new Date();
        Delivery delivery;
        try {
            delivery = waltService.createOrderAndAssignDriver(customer, restaurant, deliveryTime);
        } catch (NotSameCityException | NoAvailableDriversException | NoCustomerException e) { 
            assertEquals(e.getMessage(), ErrorMessages.NOT_SAME_CITY);
            return;
        }

        assertNotNull(delivery);
    }

    @Test
    // In this test we know we wont get an NotSameCityException\NoCustomerException!
    public void testNoFreeDrivers() {
        driverRepository.deleteAll();
        Customer customer = customerRepository.findByName("Rachmaninoff");
        Restaurant restaurant = restaurantRepository.findByName("chinese");
        Date deliveryTime = new Date();
        Delivery delivery;
        try {
            delivery = waltService.createOrderAndAssignDriver(customer, restaurant, deliveryTime);
        } catch (NotSameCityException | NoAvailableDriversException | NoCustomerException e) {
            assertEquals(e.getMessage(), ErrorMessages.NO_AVAILABLE_DRIVERS);
            return;
        }

        assertNotNull(delivery);
    }

    @Test
    public void test1Driver2Orders() throws NotSameCityException, NoAvailableDriversException, NoCustomerException {
        driverRepository.deleteAll();
        Driver ravid = new Driver("Ravid", cityRepository.findByName("Tel-Aviv"));
        driverRepository.save(ravid);
        Customer customer = customerRepository.findByName("Rachmaninoff");
        Restaurant restaurant1 = restaurantRepository.findByName("chinese");
        Restaurant restaurant2 = restaurantRepository.findByName("vegan");
        Date deliveryTime = new Date();
        waltService.createOrderAndAssignDriver(customer, restaurant1, deliveryTime);
        Delivery delivery2;
        try {
            delivery2 = waltService.createOrderAndAssignDriver(customer, restaurant2, deliveryTime);
        } catch (NotSameCityException | NoAvailableDriversException | NoCustomerException e) {
            assertEquals(e.getMessage(), ErrorMessages.NO_AVAILABLE_DRIVERS);
            return;
        }

        assertNotNull(delivery2);
    }

    @Test
    public void testNoSuchCustomer() {
        City tlv = cityRepository.findByName("Tel-Aviv");
        Restaurant restaurant = restaurantRepository.findByName("vegan");
        Customer omerAdam = new Customer("Omer Adam", tlv, "Dubai");
        Date date = new Date();
        Delivery delivery;
        try {
            delivery = waltService.createOrderAndAssignDriver(omerAdam, restaurant, date);
        }
        catch (NotSameCityException | NoAvailableDriversException | NoCustomerException e) {
            assertEquals(e.getMessage(), ErrorMessages.NO_CUSTOMER);
            return;
        }
        assertNotNull(delivery);
    }
}