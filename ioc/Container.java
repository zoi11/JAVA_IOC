package com.example.course.ioc;


import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * In this ioc implementation, I added some features, like Qualifier annotation, inject by name.
 * I hard coded the file read part, to load all the component classes into an array.
 * I modified the register method. While put class-singleton key-value pair into hashmap, I also use reflect to retrieve the interface type.
 * If the interface only have one implementation, then I will put the corresponding singleton into the map. If one interface has several implementations,
 * then I will put null value into hashmap, and while injection, it will throw exception.
 *
 * In the injectObjects method, I first checked Autowired annotation. If map return null, I will use try-catch block to see if there is a Qualifier annotation.
 * If one interface has several implementations and does not provide Qualifier annotations, which means inject by type, then it will throw exceptions.
 * And if one interface only has one implementation, then injectObjects method will use interface type to find the corresponding singleton in the hashmap.
 */
public class Container {
    private final Map<String, Object> objectFactory = new HashMap<>();

    public static void start() throws Exception{
        Container c = new Container();
        List<Class<?>> classes = c.scan();
        c.register(classes);
        c.injectObjects(classes);
        //System.out.println(c.objectFactory);
    }
    // hardcode file read
    private List<Class<?>> scan() {
        return Arrays.asList(StudentRegisterServiceImp1.class, StudentRegisterServiceImp2.class, StudentApplication.class, Starter.class, testForOneImplement.class);
    }

    private boolean register(List<Class<?>> classes) throws Exception {
        for(Class<?> clazz: classes) {

            Annotation[] annotations = clazz.getAnnotations();
            for(Annotation a: annotations) {
                if(a.annotationType() == Component.class) {
                    Object forInjection = clazz.getDeclaredConstructor(null).newInstance();
                    objectFactory.put(clazz.getSimpleName(), forInjection);
                    Class<?>[] test1= clazz.getInterfaces();
                    if(test1.length!=0){
                        String test1Name = test1[0].getSimpleName();
                        //System.out.println(test1[0].getSimpleName());
                        if(objectFactory.containsKey(test1Name)){
                            objectFactory.put(test1Name,null);
                        }else{
                            objectFactory.put(test1Name,forInjection);
                        }
                    }

                }
            }
        }
        return true;
    }

    private boolean injectObjects(List<Class<?>> classes) throws Exception{
        for(Class<?> clazz: classes) {
            Field[] fields = clazz.getDeclaredFields();
            Object curInstance = objectFactory.get(clazz.getSimpleName());
            for(Field f: fields) {
                Annotation[] annotations = f.getAnnotations();
                if(annotations.length ==0){
                    continue;
                }
                if(annotations[0].annotationType() == Autowired.class) {
                    Class<?> type = f.getType();
                    Object injectInstance = objectFactory.get(type.getSimpleName());
                    if(injectInstance==null){
                        //System.out.println(type.getSimpleName());
                        try {
                            //System.out.println(annotations[1]);

                            if(annotations[1].annotationType() != Qualifier.class){
                                Exception e = new Exception();
                                throw e;
                            }
                            Qualifier qualifierForRead = (Qualifier) annotations[1];
                            String keyString = qualifierForRead.name();
                            injectInstance = objectFactory.get(keyString);
                            f.setAccessible(true);
                            f.set(curInstance, injectInstance);
                            continue;
                        }catch(Exception e){
                            throw e;
                        }
                    }
                    f.setAccessible(true);
                    f.set(curInstance, injectInstance);
                }
            }
/*
            Constructor[] constructors = clazz.getDeclaredConstructors();
            for(Constructor constr: constructors){
                System.out.println(constr);
                Annotation[] annotations = constr.getAnnotations();
                if(annotations.length ==0){
                    continue;
                }

                if(annotations[0].annotationType() == Autowired.class){
                    //System.out.println(me);
                    Class<?>[] types = constr.getParameterTypes();
                    int len = types.length;
                    Object[] para = new Object[len];
                    for(int i = 0;i<len;i++){
                        para[i] = objectFactory.get(types[i].getSimpleName());
                    }
                    constr.setAccessible(true);
                    constr.newInstance(para);

                }

            }

 */

        }
        return true;
    }
}

//This StudentRegisterService interface has two implementations, used to test for Qualifier annotation.
@Component
interface StudentRegisterService {
}
// This testForOne interface only has one implementation. When inject by type, it will automatically inject the corresponding singleton object.
@Component
interface testForOne {
}

@Component
class testForOneImplement implements testForOne {
    @Override
    public String toString() {
        return "this is a test for an interface only having one implementation."  + "\n";
    }
}

@Component
class StudentRegisterServiceImp1 implements StudentRegisterService{
    @Override
    public String toString() {
        return "this is student register service 1 instance : " + super.toString() + "\n";
    }
}

@Component
class StudentRegisterServiceImp2 implements StudentRegisterService{
    @Override
    public String toString() {
        return "this is student register service 2 instance : " + super.toString() + "\n";
    }
}

@Component
class StudentApplication {

    @Autowired
    @Qualifier(name ="StudentRegisterServiceImp1")
    StudentRegisterService studentRegisterService1;

    @Autowired
    @Qualifier(name ="StudentRegisterServiceImp2")
    StudentRegisterService studentRegisterService2;



/*
    private StudentRegisterService studentRegisterService1;
    private StudentRegisterService studentRegisterService2;

    public StudentApplication(){
    }

    @Autowired
    public StudentApplication(StudentRegisterService ss1, StudentRegisterService ss2){
        this.studentRegisterService1 = ss1;
        this.studentRegisterService2 = ss2;
    }


*/
    @Override
    public String toString() {
        return "StudentApplication{\n" +
                "studentRegisterService=" + studentRegisterService1 +
                "\n"+
                "studentRegisterService=" + studentRegisterService2 +
                "}\n";
    }
}

@Component
class Starter {
    @Autowired
    private static StudentApplication studentApplication;
    @Autowired
    @Qualifier(name = "StudentRegisterServiceImp1")
    private static StudentRegisterService studentRegisterService1;

    @Autowired
    @Qualifier(name = "StudentRegisterServiceImp2")
    private static StudentRegisterService studentRegisterService2;
    // inject by type, testForOne interface only has one implementation, it works.
    @Autowired
    private static testForOne testVar;


    public static void main(String[] args) throws Exception{
        Container.start();
        System.out.println(studentApplication);
        System.out.println(studentRegisterService1);
        System.out.println(studentRegisterService2);
        System.out.println(testVar);

    }
}
