package payment;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import payment.config.PaymentKafkaConfig;
import payment.config.PaymentSQLConfig;
import payment.publisher.CommandProducer;
import payment.repository.AccountRepository;


public class PaymentApp {
    public static void main(String[] args) throws InterruptedException {
        ApplicationContext ctx = new AnnotationConfigApplicationContext(PaymentKafkaConfig.class, PaymentSQLConfig.class);
        AccountRepository accountRepository = ctx.getBean(AccountRepository.class);
        CommandProducer commandProducer = ctx.getBean(CommandProducer.class);
        //commandProducer.publishCreateAccountCommand();
        Thread.sleep(1000000);
    }


}
