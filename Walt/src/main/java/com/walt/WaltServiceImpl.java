package com.walt;

import com.walt.dao.DriverRepository;
import com.walt.Exceptions.NoAvailableDriversException;
import com.walt.Exceptions.NoCustomerException;
import com.walt.Exceptions.NotSameCityException;
import com.walt.dao.CustomerRepository;
import com.walt.dao.DeliveryRepository;
import javax.annotation.Resource;

import com.walt.model.*;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;




@Service
public class WaltServiceImpl implements WaltService {

    @Resource
    DriverRepository driverRepository;

    @Resource
    DeliveryRepository deliveryRepository;
    
    @Resource
    CustomerRepository customerRepository;

    @Override
    public Delivery createOrderAndAssignDriver(Customer customer, Restaurant restaurant, Date deliveryTime) throws NotSameCityException, NoAvailableDriversException, NoCustomerException {
        if (customer == null | restaurant == null | deliveryTime == null) {
            throw new IllegalArgumentException(ErrorMessages.NULL_PARAMS);
        }
        if (!customerRepository.existsByName(customer.getName())) {
            throw new NoCustomerException();
        }
        if (!customer.getCity().getName().equals(restaurant.getCity().getName())){
            throw new NotSameCityException();
        }
        List<Driver> availableDriversList = driverRepository.getAvailableDrivers(restaurant.getCity(), deliveryTime);
        if (availableDriversList.size() == 0) {
            throw new NoAvailableDriversException();
        }
        Driver availableDriver = getLeastBusyDriver(availableDriversList);
        Delivery newDelivery = new Delivery(availableDriver, restaurant, customer, deliveryTime);
        newDelivery.setDistance(ThreadLocalRandom.current().nextDouble(0, 20));
        deliveryRepository.save(newDelivery);
        return newDelivery;
    }

    @Override
    public List<DriverDistance> getDriverRankReport() {
        return driverRepository.getDistancesByDriver();
    }

    @Override
    public List<DriverDistance> getDriverRankReportByCity(City city) {
        return driverRepository.getDriverDistancesByCity(city);
    }


    //Here we look for the least busy driver (number of deliveries-wise) and return him/her.
    public Driver getLeastBusyDriver(List<Driver> availabDrivers){
        int min = deliveryRepository.findAllDeliveriesByDriver(availabDrivers.get(0)).size();
        int index = 0;
        for (int i = 1; i < availabDrivers.size(); i++){
            int numOfDeliveries = deliveryRepository.findAllDeliveriesByDriver(availabDrivers.get(i)).size();
            if (numOfDeliveries < min){
                min = numOfDeliveries;
                index = i;
            }
        }
        return availabDrivers.get(index);
    }
}
