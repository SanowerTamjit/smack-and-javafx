/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package smackapi.einbinden;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.pubsub.AccessModel;
import org.jivesoftware.smackx.pubsub.ConfigureForm;
import org.jivesoftware.smackx.pubsub.FormType;
import org.jivesoftware.smackx.pubsub.Item;
import org.jivesoftware.smackx.pubsub.ItemPublishEvent;
import org.jivesoftware.smackx.pubsub.LeafNode;
import org.jivesoftware.smackx.pubsub.PubSubManager;
import org.jivesoftware.smackx.pubsub.PublishModel;
import org.jivesoftware.smackx.pubsub.listener.ItemEventListener;

/**
 *
 * @author kaisky89
 */
public class SmackAPIController implements Initializable {

    @FXML private Button button;
    @FXML private TextField input;
    @FXML private ListView list;
    private XMPPConnection connection;
    private String host = "localhost";
    private String user = "user1@localhost";
    private String passwd = "123";
    private PubSubManager mgr;
    private LeafNode node;

    @FXML
    private void handleButtonSend(ActionEvent event) {
        String message = input.getText();
        publish(message);
        System.out.println("Sende Nachricht: \"" + message + "\"");
        input.setText("");
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        connectXmpp();
        node = connectLeaf();
        
        node.addItemEventListener(new EventListenerNewItem(list));
        try {
            node.subscribe(user);
        } catch (XMPPException ex) {
            System.err.println(ex);
            ex.printStackTrace(System.err);
        }
    }

    private void connectXmpp() {
        connection = new XMPPConnection(host);
        try {
            connection.connect();
            connection.login(user, passwd);
        } catch (XMPPException ex) {
            System.err.println(ex);
            ex.printStackTrace(System.err);
        }
    }

    private LeafNode connectLeaf() {
        // Create a pubsub manager using an existing Connection
        LeafNode returnNode = null;
        mgr = new PubSubManager(connection);
        try {
            // Get the node
            returnNode = mgr.getNode("testNode");
        } catch (XMPPException ex) {
            System.err.println("konnte testNode nicht finden, erstelle ihn.");
            try {
                // Create the node
                returnNode = mgr.createNode("testNode");
            } catch (XMPPException ex1) {
                System.err.println(ex);
                ex.printStackTrace(System.err);
            }
            ConfigureForm form = new ConfigureForm(FormType.submit);
            form.setAccessModel(AccessModel.open);
            form.setDeliverPayloads(false);
            form.setNotifyRetract(true);
            form.setPersistentItems(true);
            form.setPublishModel(PublishModel.open);
            try {
                returnNode.sendConfigurationForm(form);
            } catch (XMPPException ex1) {
                System.err.println(ex);
                ex.printStackTrace(System.err);
            }
        }
        return returnNode;
    }

    private void publish(String id) {
        try {
            // Publish an Item with the specified id
            node.send(new Item(id));
        } catch (XMPPException ex) {
            System.err.println(ex);
            ex.printStackTrace(System.err);
        }
    }

    private static class EventListenerNewItem implements ItemEventListener {

        private ListView list;
        
        private EventListenerNewItem(ListView list) {
            this.list = list;
        }

        @Override
        public void handlePublishedItems(ItemPublishEvent items) {
            for (Object oItem : items.getItems()) {
                final Item item = (Item) oItem;
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("Empfange Item: \"" + item.getId() + "\"");
                        list.getItems().add(item.getId());
                    }
                });
            }
        }
    }
}
