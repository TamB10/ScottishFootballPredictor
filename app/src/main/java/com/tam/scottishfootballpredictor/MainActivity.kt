package com.tam.scottishfootballpredictor

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import com.tam.scottishfootballpredictor.model.PredictionResult

class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: PredictorViewModel

    private lateinit var leagueSpinner: AutoCompleteTextView
    private lateinit var homeTeamSpinner: AutoCompleteTextView
    private lateinit var awayTeamSpinner: AutoCompleteTextView
    private lateinit var predictButton: MaterialButton
    private lateinit var clearButton: MaterialButton
    private lateinit var predictionResultText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize ViewModel
        viewModel = ViewModelProvider(this, PredictorViewModelFactory(application))
            .get(PredictorViewModel::class.java)

        initializeViews()
        setupSpinners()
        setupButtons()
        observeViewModel()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_update_stats -> {
                updateStats()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun updateStats() {
        viewModel.checkForUpdates()
        Toast.makeText(this, "Checking for updates...", Toast.LENGTH_SHORT).show()
    }

    private fun initializeViews() {
        leagueSpinner = findViewById(R.id.leagueSpinner)
        homeTeamSpinner = findViewById(R.id.homeTeamSpinner)
        awayTeamSpinner = findViewById(R.id.awayTeamSpinner)
        predictButton = findViewById(R.id.predictButton)
        clearButton = findViewById(R.id.clearButton)
        predictionResultText = findViewById(R.id.predictionResultText)
    }

    private fun setupSpinners() {
        ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            viewModel.getLeagues()
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            leagueSpinner.setAdapter(adapter)
        }

        leagueSpinner.setOnItemClickListener { parent, _, position, _ ->
            val league = parent.getItemAtPosition(position).toString()
            updateTeamsSpinners(league)
        }
    }

    private fun updateTeamsSpinners(league: String) {
        val teams = viewModel.getTeamsForLeague(league)
        val teamAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            teams
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        homeTeamSpinner.setAdapter(teamAdapter)
        awayTeamSpinner.setAdapter(teamAdapter)
    }

    private fun setupButtons() {
        predictButton.setOnClickListener {
            val homeTeam = homeTeamSpinner.text.toString()
            val awayTeam = awayTeamSpinner.text.toString()
            val league = leagueSpinner.text.toString()

            if (homeTeam.isNotEmpty() && awayTeam.isNotEmpty() && league.isNotEmpty()) {
                if (homeTeam == awayTeam) {
                    Toast.makeText(this, "Please select different teams", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                viewModel.predictScore(homeTeam, awayTeam, league)
            } else {
                Toast.makeText(this, "Please select both teams and league", Toast.LENGTH_SHORT).show()
            }
        }

        clearButton.setOnClickListener {
            viewModel.clearPrediction()
            homeTeamSpinner.text.clear()
            awayTeamSpinner.text.clear()
        }
    }

    private fun observeViewModel() {
        viewModel.predictionResult.observe(this) { result ->
            updatePredictionDisplay(result)
        }
    }

    private fun updatePredictionDisplay(result: PredictionResult?) {
        if (result == null) {
            predictionResultText.text = "Select teams and click Predict"
            return
        }

        val homeTeam = homeTeamSpinner.text.toString()
        val awayTeam = awayTeamSpinner.text.toString()

        val predictionText = """
            $homeTeam ${result.homeGoals} - ${result.awayGoals} $awayTeam
            
            Expected Goals: ${result.homeXg} - ${result.awayXg}
            
            Win Probability: 
            Home: ${result.homeWinProb}%
            Draw: ${result.drawProb}%
            Away: ${result.awayWinProb}%
        """.trimIndent()

        predictionResultText.text = predictionText
    }
}