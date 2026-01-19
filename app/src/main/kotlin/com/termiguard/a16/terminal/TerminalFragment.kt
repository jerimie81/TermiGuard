package com.termiguard.a16.terminal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.termux.view.TerminalView

/**
 * UI Fragment hosting the Termux-view emulator.
 * Handles rendering for the SSH session.
 */
class TerminalFragment : Fragment() {

    private var terminalView: TerminalView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Create the TerminalView programmatically or via XML
        terminalView = TerminalView(requireContext(), null)
        
        // Configuration for SM-A165M 90Hz screen
        terminalView?.apply {
            setTextSize(14)
            // Additional rendering optimizations can be added here
        }

        return terminalView
    }

    override fun onResume() {
        super.onResume()
        // Focus the terminal to capture key events
        terminalView?.requestFocus()
    }

    fun getTerminalView(): TerminalView? = terminalView
}
