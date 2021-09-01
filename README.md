# JAVA_IOC

 * In this ioc implementation, I added some features, like Qualifier annotation, inject by name.
 * I hard coded the file read part, to load all the component classes into an array.
 * I modified the register method. While put class-singleton key-value pair into hashmap, I also use reflect to retrieve the interface type.
 * If the interface only have one implementation, then I will put the corresponding singleton into the map. If one interface has several implementations,
 * then I will put null value into hashmap, and while injection, it will throw exception.
 *
 * In the injectObjects method, I first checked Autowired annotation. If map return null, I will use try-catch block to see if there is a Qualifier annotation.
 * If one interface has several implementations and does not provide Qualifier annotations, which means inject by type, then it will throw exceptions.
 * And if one interface only has one implementation, then injectObjects method will use interface type to find the corresponding singleton in the hashmap.
 * 
