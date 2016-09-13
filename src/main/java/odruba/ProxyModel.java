package odruba;

import org.apache.jena.rdf.model.Model;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class ProxyModel implements InvocationHandler {

    private Model model;

    public ProxyModel(Model model) {
        this.model = model;
    }

    public Object getModel() {
        return model;
    }

    public void setModel(Model model) {
        this.model = model;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] params) throws Throwable {
        Object result = method.invoke(model, params);
        return result;
    }
}
