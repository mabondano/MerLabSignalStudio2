package com.merlab.signals.examples;

import com.merlab.signals.data.DataLoader;
import com.merlab.signals.data.DataLoaderFactory;
import com.merlab.signals.data.DataLoaderFactory.Type;
import com.merlab.signals.data.DataLoaderConfig;
import com.merlab.signals.data.DataSet;
import com.merlab.signals.core.Signal;
import com.merlab.signals.nn.manager.NeuralNetworkManager;
import com.merlab.signals.nn.model.RegressionModel;
import com.merlab.signals.nn.model.SimpleRegressionModel;
import com.merlab.signals.nn.processor.LinearProcessor;
import com.merlab.signals.nn.processor.NeuralNetworkProcessor;
import com.merlab.signals.nn.processor.SimpleLinearProcessor;
import com.merlab.signals.nn.trainer.LinearRegressionTrainer;
import com.merlab.signals.nn.trainer.RegressionTrainer;
import com.merlab.signals.nn.trainer.simple.SimpleLinearRegressionTrainer;
import com.merlab.signals.nn.trainer.simple.SimpleLinearRegressionTrainer4;
import com.merlab.signals.nn.trainer.simple.SimpleRegressionTrainer;
import com.merlab.signals.persistence.DatabaseManager;
import com.merlab.signals.core.SignalStack;

import java.nio.file.Paths;
import java.util.List;

public class RealDataFactoryExample {

    public static void main(String[] args) throws Exception {
        // 1. Configurar el DataLoader para CSV
        DataLoaderConfig cfg = new DataLoaderConfig();
        cfg.setCsvPath("src/main/resources/data/house_prices_csv_Sample.csv");
        cfg.setNumInputs(3);
        cfg.setNumTargets(1);

        DataLoader loader = DataLoaderFactory.create(Type.CSV, cfg);

        // 2. Cargar el DataSet
        DataSet data = loader.load();
        List<Signal> inputs  = data.getInputs();
        List<Signal> targets = data.getTargets();

        // 3. Entrenar regresión lineal
        SimpleRegressionTrainer trainer = new SimpleLinearRegressionTrainer4();
        SimpleRegressionModel model = trainer.train(
            inputs, targets, Paths.get("model_factory.txt")
        );

        // 4. Preparar el procesador lineal y el manager
        NeuralNetworkProcessor proc = 
            new SimpleLinearProcessor(model.getWeights(), model.getBias());
        SignalStack stack = new SignalStack();
        DatabaseManager db = new DatabaseManager("jdbc:mariadb://localhost:3306/test", "root", "root"); // tu implementación
        NeuralNetworkManager nnManager = new NeuralNetworkManager(stack, proc, db);

        // 5. Inferencia de ejemplo
        Signal sample = new Signal();
        sample.add(2000.0);  // sqft
        sample.add(3.0);     // bedrooms
        sample.add(20.0);    // age
        stack.push(sample);

        Signal prediction = nnManager.runInference();
        System.out.println("Predicción precio [factory]: " + prediction.getValues());

        // 6. Persistir
        nnManager.saveResult();
    }
}
