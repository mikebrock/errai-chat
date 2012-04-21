package org.cdidemo2.client.local;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import org.jboss.errai.ioc.client.container.IOCBeanManager;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Mike Brock
 */
public class ChatLogin extends Composite {
  @Inject UiBinder<Widget, ChatLogin> uiBinder;
  @Inject IOCBeanManager beanManger;

  @UiField Label welcomeMessage;
  @UiField DecoratorPanel panel;
  @UiField TextBox nameBox;
  @UiField Button goButton;

  private final List<Runnable> onClick = new ArrayList<Runnable>();

  @PostConstruct
  private void buildWidget() {
    initWidget(uiBinder.createAndBindUi((ChatLogin) beanManger.getActualBeanReference(this)));
  }

  @UiHandler("nameBox")
  void onNameBoxKeypress(KeyUpEvent event) {
    if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
      onGoButtonClick(null);
    }
  }

  @UiHandler("goButton")
  void onGoButtonClick(ClickEvent event) {
    for (Runnable runnable : onClick) {
      runnable.run();
    }
    setVisible(false);
  }

  public String getNick() {
    return nameBox.getText();
  }

  public void addOnClickCallback(final Runnable runnable) {
    onClick.add(runnable);
  }

  public void displayWithMessage(String message) {
    welcomeMessage.setText(message);
    setVisible(true);
    center();
  }

  public void center() {
    int width = panel.getOffsetWidth();
    int height = panel.getOffsetHeight();
    double left = (Window.getClientWidth() - width) / 2;
    double top = (Window.getClientHeight() - height) / 2;

    panel.getElement().getStyle().setPosition(Style.Position.ABSOLUTE);
    panel.getElement().getStyle().setLeft(left, Style.Unit.PX);
    panel.getElement().getStyle().setTop(top, Style.Unit.PX);
  }
}
