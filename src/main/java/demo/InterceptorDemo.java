package demo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * @author Stepan Koltsov
 */
@Controller
@Import({ InterceptorDemo.InterceptorConfigurer.class })
@EnableAutoConfiguration
public class InterceptorDemo {

    public static class PrintInterceptor implements AsyncHandlerInterceptor {

        @Override
        public void afterConcurrentHandlingStarted(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
            System.out.println("afterConcurrentHandlingStarted " + request.getRequestURI());
        }

        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
            System.out.println("preHandle " + request.getRequestURI());
            return true;
        }

        @Override
        public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
            System.out.println("postHandle " + request.getRequestURI());
        }

        @Override
        public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
            System.out.println("afterCompletion " + request.getRequestURI());
        }
    }

    @RequestMapping("/")
    @ResponseBody
    public String first() {
        return "OK";
    }

    @RequestMapping("/async")
    @ResponseBody
    public DeferredResult<String> async(@RequestParam(value = "s", defaultValue = "1000") final long s) {
        final DeferredResult<String> r = new DeferredResult<>();
        r.onTimeout(new Runnable() {
            @Override
            public void run() {
                System.out.println("DeferredResult.onTimeout");
            }
        });
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(s);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                System.out.println("returning");
                r.setResult("aaaa");

            }
        }).start();
        return r;
    }

    @Component
    public static class InterceptorConfigurer extends WebMvcConfigurerAdapter {
        @Override
        public void addInterceptors(InterceptorRegistry registry) {
            registry.addInterceptor(new PrintInterceptor());
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(InterceptorDemo.class, args);
    }
}
