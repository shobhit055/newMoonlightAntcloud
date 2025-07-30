package com.antcloud.app.computers;

import com.antcloud.app.nvstream.http.ComputerDetails;

public interface ComputerManagerListener {
    void notifyComputerUpdated(ComputerDetails details);
}
