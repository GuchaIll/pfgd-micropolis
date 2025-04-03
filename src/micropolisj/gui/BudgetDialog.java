// This file is part of MicropolisJ.
// Copyright (C) 2013 Jason Long
// Portions Copyright (C) 1989-2007 Electronic Arts Inc.
//
// MicropolisJ is free software; you can redistribute it and/or modify
// it under the terms of the GNU GPLv3, with additional terms.
// See the README file, included in this distribution, for details.

package micropolisj.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;

import micropolisj.engine.*;
import static micropolisj.gui.MainWindow.formatFunds;
import static micropolisj.gui.MainWindow.formatGameDate;

public class BudgetDialog extends JDialog
{
	Micropolis engine;

	JSpinner taxRateEntry;
	JSpinner salesTaxRateEntry;
	JSpinner taxIncomeRateEntry;
	JSpinner industryTaxRateEntry;
	JSpinner housingTaxRateEntry;
	JSpinner exportTaxRateEntry;

	int origTaxRate;
	double origRoadPct;
	double origFirePct;
	double origPolicePct;

	JLabel gdpValueLabel = new JLabel();
	JLabel economicCycleLabel = new JLabel("Economic Cycle: NORMAL");
	JLabel loanAmountLabel = new JLabel("Total Loan: $0");
	JTextField loanAmountField = new JTextField("0", 10);
	JLabel loanRepaymentLabel = new JLabel("Annual Repayment: $0");
	JButton takeLoanButton = new JButton("Take Loan");


	JLabel roadFundRequest = new JLabel();
	JLabel roadFundAlloc = new JLabel();
	JSlider roadFundEntry;


	JLabel policeFundRequest = new JLabel();
	JLabel policeFundAlloc = new JLabel();
	JSlider policeFundEntry;

	JLabel fireFundRequest = new JLabel();
	JLabel fireFundAlloc = new JLabel();
	JSlider fireFundEntry;

	JLabel taxRevenueLbl = new JLabel();

	static ResourceBundle strings = MainWindow.strings;

	JCheckBox autoBudgetBtn = new JCheckBox(strings.getString("budgetdlg.auto_budget"));
	JCheckBox pauseBtn = new JCheckBox(strings.getString("budgetdlg.pause_game"));

	private void applyChange()
	{
		int newTaxRate = ((Number) taxRateEntry.getValue()).intValue();
		int newSalesTaxRate = ((Number) salesTaxRateEntry.getValue()).intValue();
		int newTaxIncomeRate = ((Number) taxIncomeRateEntry.getValue()).intValue();
		int newIndustryTaxRate = ((Number) industryTaxRateEntry.getValue()).intValue();
		int newHousingTaxRate = ((Number) housingTaxRateEntry.getValue()).intValue();
		int newExportTaxRate = ((Number) exportTaxRateEntry.getValue()).intValue();

		int newRoadPct = ((Number) roadFundEntry.getValue()).intValue();
		int newPolicePct = ((Number) policeFundEntry.getValue()).intValue();
		int newFirePct = ((Number) fireFundEntry.getValue()).intValue();

		engine.cityTax = newTaxRate;
		engine.salesTax = newSalesTaxRate;
		engine.incomeTax = newTaxIncomeRate;
		engine.industryTax = newIndustryTaxRate;
		engine.housingTax = newHousingTaxRate;
		engine.exportImportTax = newExportTaxRate;

		engine.roadPercent = (double)newRoadPct / 100.0;
		engine.policePercent = (double)newPolicePct / 100.0;
		engine.firePercent = (double)newFirePct / 100.0;

		loadBudgetNumbers(false);
	}


	private void loadBudgetNumbers(boolean updateEntries)
	{
		BudgetNumbers b = engine.generateBudget();
		if (updateEntries)
		{
		taxRateEntry.setValue(b.taxRate);
		salesTaxRateEntry.setValue(b.salesTaxRate);
		taxIncomeRateEntry.setValue(b.taxIncomeRate);
		industryTaxRateEntry.setValue(b.IndustryTaxRate);
		housingTaxRateEntry.setValue(b.housingTaxRate);
		exportTaxRateEntry.setValue(b.exportTaxRate);

		loanAmountLabel.setText("Total Loan: " + formatFunds(engine.loanAmount));
		loanRepaymentLabel.setText("Annual Repayment: " + formatFunds(engine.loanRepaymentPerYear));


		roadFundEntry.setValue((int)Math.round(b.roadPercent*100.0));
		policeFundEntry.setValue((int)Math.round(b.policePercent*100.0));
		fireFundEntry.setValue((int)Math.round(b.firePercent*100.0));
		}

		taxRevenueLbl.setText(formatFunds(b.taxIncome));

		roadFundRequest.setText(formatFunds(b.roadRequest));
		roadFundAlloc.setText(formatFunds(b.roadFunded));

		policeFundRequest.setText(formatFunds(b.policeRequest));
		policeFundAlloc.setText(formatFunds(b.policeFunded));

		fireFundRequest.setText(formatFunds(b.fireRequest));
		fireFundAlloc.setText(formatFunds(b.fireFunded));

		int gdp = engine.calculateGDP();
		gdpValueLabel.setText(formatFunds(gdp));
		economicCycleLabel.setText("Economic Cycle: " + engine.currentCycle.name());
	}

	static void adjustSliderSize(JSlider slider)
	{
		Dimension sz = slider.getPreferredSize();
		slider.setPreferredSize(
			new Dimension(80, sz.height)
			);
	}

	private void onTakeLoanClicked() {
		String input = JOptionPane.showInputDialog(this, "Enter loan amount:", "Take Loan", JOptionPane.PLAIN_MESSAGE);
		if (input != null) {
			try {
				int amount = Integer.parseInt(input);
				if (amount > 0) {
					engine.takeLoan(amount);
					loadBudgetNumbers(true);
					loanAmountLabel.setText("Total Loan: " + formatFunds(engine.loanAmount));
					loanRepaymentLabel.setText("Annual Repayment: " + formatFunds(engine.loanRepaymentPerYear));
				} else {
					JOptionPane.showMessageDialog(this, "Loan amount must be positive.", "Error", JOptionPane.ERROR_MESSAGE);
				}
			} catch (NumberFormatException e) {
				JOptionPane.showMessageDialog(this, "Invalid loan amount.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	public BudgetDialog(Window owner, Micropolis engine)
	{
		super(owner);
		setTitle(strings.getString("budgetdlg.title"));

		this.engine = engine;
		this.origTaxRate = engine.cityTax;
		this.origRoadPct = engine.roadPercent;
		this.origFirePct = engine.firePercent;
		this.origPolicePct = engine.policePercent;

		int gdp = engine.calculateGDP();
		gdpValueLabel.setText(formatFunds(gdp));

		economicCycleLabel.setText("Economic Cycle: " + engine.currentCycle.name());
    	
		// give text fields of the fund-level spinners a minimum size
		taxRateEntry = new JSpinner(new SpinnerNumberModel(7,0,20,1));
		salesTaxRateEntry = new JSpinner(new SpinnerNumberModel(0,0,20,1));
		taxIncomeRateEntry = new JSpinner(new SpinnerNumberModel(0,0,20,1));
		industryTaxRateEntry = new JSpinner(new SpinnerNumberModel(0,0,20,1));
		housingTaxRateEntry = new JSpinner(new SpinnerNumberModel(0,0,20,1));
		exportTaxRateEntry = new JSpinner(new SpinnerNumberModel(0,0,20,1));

		// widgets to set funding levels
		roadFundEntry = new JSlider(JSlider.HORIZONTAL, 0, 100, 100);
		adjustSliderSize(roadFundEntry);
		fireFundEntry = new JSlider(JSlider.HORIZONTAL, 0, 100, 100);
		adjustSliderSize(fireFundEntry);
		policeFundEntry = new JSlider(JSlider.HORIZONTAL, 0, 100, 100);
		adjustSliderSize(policeFundEntry);

		ChangeListener change = new ChangeListener() {
		public void stateChanged(ChangeEvent ev) {
			applyChange();
		}
		};
		taxRateEntry.addChangeListener(change);
		salesTaxRateEntry.addChangeListener(change);
		taxIncomeRateEntry.addChangeListener(change);
		industryTaxRateEntry.addChangeListener(change);
		housingTaxRateEntry.addChangeListener(change);
		exportTaxRateEntry.addChangeListener(change);

		roadFundEntry.addChangeListener(change);
		fireFundEntry.addChangeListener(change);
		policeFundEntry.addChangeListener(change);

		Box mainBox = new Box(BoxLayout.Y_AXIS);
		mainBox.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
		add(mainBox, BorderLayout.CENTER);

		mainBox.add(makeTaxPane());

		JPanel loanPane = new JPanel(new GridBagLayout());
        loanPane.setBorder(BorderFactory.createTitledBorder("Government Loan"));
		
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(5, 5, 5, 5); // Add padding: top, left, bottom, right
		c.anchor = GridBagConstraints.WEST;

		c.gridx = 0;
		c.gridy = 0;
        loanPane.add(loanAmountLabel, c);

		c.gridy = 1;
        loanPane.add(loanRepaymentLabel, c);

		c.gridy = 2;
		c.gridwidth = 2; // Span two columns
		c.anchor = GridBagConstraints.CENTER; 
        JButton loanButton = new JButton("Take Loan");
        loanButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                onTakeLoanClicked();
            }});

        loanPane.add(loanButton);
		mainBox.add(loanPane);

		JSeparator sep = new JSeparator(SwingConstants.HORIZONTAL);
		mainBox.add(sep);

		mainBox.add(makeFundingRatesPane());

		JSeparator sep1 = new JSeparator(SwingConstants.HORIZONTAL);
		mainBox.add(sep1);

		mainBox.add(makeBalancePane());

		JSeparator sep2 = new JSeparator(SwingConstants.HORIZONTAL);
		mainBox.add(sep2);

		mainBox.add(makeOptionsPane());
         
		

		JPanel buttonPane = new JPanel();
		add(buttonPane, BorderLayout.SOUTH);

		JButton continueBtn = new JButton(strings.getString("budgetdlg.continue"));
		continueBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				onContinueClicked();
			}});
		buttonPane.add(continueBtn);

		JButton resetBtn = new JButton(strings.getString("budgetdlg.reset"));
		resetBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				onResetClicked();
			}});
		buttonPane.add(resetBtn);

		

		loadBudgetNumbers(true);
		setAutoRequestFocus_compat(false);
		pack();
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(owner);
		getRootPane().registerKeyboardAction(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				dispose();
			}},
			KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
			JComponent.WHEN_IN_FOCUSED_WINDOW);
	}

	private void setAutoRequestFocus_compat(boolean v)
	{
		try
		{
			if (super.getClass().getMethod("setAutoRequestFocus", boolean.class) != null) {
				super.setAutoRequestFocus(v);
			}
		}
		catch (NoSuchMethodException e) {
			// ok to ignore
		}
	}

	private JComponent makeFundingRatesPane()
	{
		JPanel fundingRatesPane = new JPanel(new GridBagLayout());
		fundingRatesPane.setBorder(BorderFactory.createEmptyBorder(8,0,8,0));

		GridBagConstraints c0 = new GridBagConstraints();
		c0.gridx = 0;
		c0.weightx = 0.25;
		c0.anchor = GridBagConstraints.WEST;
		GridBagConstraints c1 = new GridBagConstraints();
		c1.gridx = 1;
		c1.weightx = 0.25;
		c1.anchor = GridBagConstraints.EAST;
		GridBagConstraints c2 = new GridBagConstraints();
		c2.gridx = 2;
		c2.weightx = 0.5;
		c2.anchor = GridBagConstraints.EAST;
		GridBagConstraints c3 = new GridBagConstraints();
		c3.gridx = 3;
		c3.weightx = 0.5;
		c3.anchor = GridBagConstraints.EAST;

		c1.gridy = c2.gridy = c3.gridy = 0;
		fundingRatesPane.add(new JLabel(strings.getString("budgetdlg.funding_level_hdr")), c1);
		fundingRatesPane.add(new JLabel(strings.getString("budgetdlg.requested_hdr")), c2);
		fundingRatesPane.add(new JLabel(strings.getString("budgetdlg.allocation_hdr")), c3);

		c0.gridy = c1.gridy = c2.gridy = c3.gridy = 1;
		fundingRatesPane.add(new JLabel(strings.getString("budgetdlg.road_fund")), c0);
		fundingRatesPane.add(roadFundEntry, c1);
		fundingRatesPane.add(roadFundRequest, c2);
		fundingRatesPane.add(roadFundAlloc, c3);

		c0.gridy = c1.gridy = c2.gridy = c3.gridy = 2;
		fundingRatesPane.add(new JLabel(strings.getString("budgetdlg.police_fund")), c0);
		fundingRatesPane.add(policeFundEntry, c1);
		fundingRatesPane.add(policeFundRequest, c2);
		fundingRatesPane.add(policeFundAlloc, c3);

		c0.gridy = c1.gridy = c2.gridy = c3.gridy = 3;
		fundingRatesPane.add(new JLabel(strings.getString("budgetdlg.fire_fund")), c0);
		fundingRatesPane.add(fireFundEntry, c1);
		fundingRatesPane.add(fireFundRequest, c2);
		fundingRatesPane.add(fireFundAlloc, c3);

		return fundingRatesPane;
	}

	private JComponent makeOptionsPane()
	{
		JPanel optionsPane = new JPanel(new GridBagLayout());
		optionsPane.setBorder(BorderFactory.createEmptyBorder(8,0,0,0));

		GridBagConstraints c0 = new GridBagConstraints();
		GridBagConstraints c1 = new GridBagConstraints();

		c0.gridx = 0;
		c1.gridx = 1;
		c0.anchor = c1.anchor = GridBagConstraints.WEST;
		c0.gridy = c1.gridy = 0;
		c0.weightx = c1.weightx = 0.5;
		optionsPane.add(autoBudgetBtn, c0);
		optionsPane.add(pauseBtn, c1);

		autoBudgetBtn.setSelected(engine.autoBudget);
		pauseBtn.setSelected(engine.simSpeed == Speed.PAUSED);

		return optionsPane;
	}

	private JComponent makeTaxPane()
	{
		JPanel pane = new JPanel(new GridBagLayout());
		pane.setBorder(BorderFactory.createEmptyBorder(0,0,8,0));

		GridBagConstraints c0 = new GridBagConstraints();
		GridBagConstraints c1 = new GridBagConstraints();
		GridBagConstraints c2 = new GridBagConstraints();

		c0.gridx = 0;
		c0.anchor = GridBagConstraints.WEST;
		c0.weightx = 0.25;
		c1.gridx = 1;
		c1.anchor = GridBagConstraints.EAST;
		c1.weightx = 0.25;
		c2.gridx = 2;
		c2.anchor = GridBagConstraints.EAST;
		c2.weightx = 0.5;

		c0.gridy = c1.gridy = c2.gridy = 0;
		pane.add(new JLabel(strings.getString("budgetdlg.tax_rate_hdr")), c1);
		pane.add(new JLabel(strings.getString("budgetdlg.annual_receipts_hdr")), c2);

		c0.gridy = c1.gridy = c2.gridy = 1;
		pane.add(new JLabel(strings.getString("budgetdlg.tax_revenue")), c0);
		pane.add(taxRateEntry, c1);
		pane.add(taxRevenueLbl, c2);

		// Add Sales Tax Rate Spinner
		//c0.gridy = 1;
		//c1.gridy = 1;
		//pane.add(new JLabel("Sales Tax Rate:"), c0);
		//pane.add(salesTaxRateEntry, c1);
	
		// Add Housing Tax Rate Spinner
		//c0.gridy = 2;
		//c1.gridy = 2;
		//pane.add(new JLabel("Housing Tax Rate:"), c0);
		//pane.add(housingTaxRateEntry, c1);
	
		// Add Industry Tax Rate Spinner
		//c0.gridy = 3;
		//c1.gridy = 3;
		///pane.add(new JLabel("Industry Tax Rate:"), c0);
		//pane.add(industryTaxRateEntry, c1);
	
		// Add Export/Import Tax Rate Spinner
		//c0.gridy = 4;
		//c1.gridy = 4;
		//pane.add(new JLabel("Export/Import Tax Rate:"), c0);
		//pane.add(exportTaxRateEntry, c1);
	
		return pane;
	}

	private void onContinueClicked()
	{

		if (autoBudgetBtn.isSelected() != engine.autoBudget) {
			engine.toggleAutoBudget();
		}
		if (pauseBtn.isSelected() && engine.simSpeed != Speed.PAUSED) {
			engine.setSpeed(Speed.PAUSED);
		}
		else if (!pauseBtn.isSelected() && engine.simSpeed == Speed.PAUSED) {
			engine.setSpeed(Speed.NORMAL);
		}
        
		dispose();
	}

	private void onResetClicked()
	{
		engine.cityTax = this.origTaxRate;
		engine.roadPercent = this.origRoadPct;
		engine.firePercent = this.origFirePct;
		engine.policePercent = this.origPolicePct;
		loadBudgetNumbers(true);
	}

	private JComponent makeBalancePane()
	{
		JPanel balancePane = new JPanel(new GridBagLayout());
		balancePane.setBorder(BorderFactory.createEmptyBorder(8,24,8,24));

		GridBagConstraints c0 = new GridBagConstraints();
		GridBagConstraints c1 = new GridBagConstraints();

		c0.anchor = GridBagConstraints.WEST;
		c0.weightx = 0.5;
		c0.gridx = 0;
		c0.gridy = 0;

		JLabel thLbl = new JLabel(strings.getString("budgetdlg.period_ending"));
		Font origFont = thLbl.getFont();
		Font headFont = origFont.deriveFont(Font.ITALIC);
		thLbl.setFont(headFont);
		thLbl.setForeground(Color.MAGENTA);
		balancePane.add(thLbl, c0);

		c0.gridy++;
		balancePane.add(new JLabel(strings.getString("budgetdlg.cash_begin")), c0);
		c0.gridy++;
		balancePane.add(new JLabel(strings.getString("budgetdlg.taxes_collected")), c0);
		c0.gridy++;
		balancePane.add(new JLabel(strings.getString("budgetdlg.capital_expenses")), c0);
		c0.gridy++;
		balancePane.add(new JLabel(strings.getString("budgetdlg.operating_expenses")), c0);
		c0.gridy++;
		balancePane.add(new JLabel(strings.getString("budgetdlg.cash_end")), c0);

		//Add GDP Label
		c0.gridy++;
		balancePane.add(new JLabel("City GDP:"), c0);

		c1.anchor = GridBagConstraints.EAST;
		c1.weightx = 0.25;
		c1.gridx = 1;
		c1.gridy = c0.gridy;
		balancePane.add(gdpValueLabel, c1);

		 // Add Economic Cycle Label
		c0.gridy++;
		 c1.weightx = 0.25;
		 c1.gridx = 1;
		 c1.gridy = c0.gridy;
		balancePane.add(economicCycleLabel, c1);


		c1.anchor = GridBagConstraints.EAST;
		c1.weightx = 0.25;
		c1.gridx = 0;

		for (int i = 0; i < 2; i++) {

			if (i + 1 >= engine.financialHistory.size()) {
				break;
			}

			Micropolis.FinancialHistory f = engine.financialHistory.get(i);
			Micropolis.FinancialHistory fPrior = engine.financialHistory.get(i+1);
			int cashFlow = f.totalFunds - fPrior.totalFunds;
			int capExpenses = -(cashFlow - f.taxIncome + f.operatingExpenses);

			c1.gridx++;
			c1.gridy = 0;

			thLbl = new JLabel(formatGameDate(f.cityTime-1));
			thLbl.setFont(headFont);
			thLbl.setForeground(Color.MAGENTA);
			balancePane.add(thLbl, c1);

			c1.gridy++;
			JLabel previousBalanceLbl = new JLabel();
			previousBalanceLbl.setText(formatFunds(fPrior.totalFunds));
			balancePane.add(previousBalanceLbl, c1);

			c1.gridy++;
			JLabel taxIncomeLbl = new JLabel();
			taxIncomeLbl.setText(formatFunds(f.taxIncome));
			balancePane.add(taxIncomeLbl, c1);

			c1.gridy++;
			JLabel capExpensesLbl = new JLabel();
			capExpensesLbl.setText(formatFunds(capExpenses));
			balancePane.add(capExpensesLbl, c1);

			c1.gridy++;
			JLabel opExpensesLbl = new JLabel();
			opExpensesLbl.setText(formatFunds(f.operatingExpenses));
			balancePane.add(opExpensesLbl, c1);

			c1.gridy++;
			JLabel newBalanceLbl = new JLabel();
			newBalanceLbl.setText(formatFunds(f.totalFunds));
			balancePane.add(newBalanceLbl, c1);
		}

		return balancePane;
	}
}
