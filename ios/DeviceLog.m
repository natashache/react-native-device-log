#import "DeviceLog.h"


@implementation DeviceLog

RCT_EXPORT_MODULE()

int const RESULT_SUCCESS = 0;
int const RESULT_FAILED = -1;

NSString *logFileName = nil;

RCT_EXPORT_METHOD(startDeviceLog: (RCTResponseSenderBlock)callback)
{
    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    NSString *documentsDirectory = [paths objectAtIndex:0];
    NSString *fileName = [NSString stringWithFormat:@"device-log-%@.log", [NSDate date]];
    NSString *logFilePath = [documentsDirectory stringByAppendingPathComponent:fileName];
    freopen([logFilePath cStringUsingEncoding:NSASCIIStringEncoding], "a+", stderr);
    
    logFileName = fileName;
    callback(@[[NSNumber numberWithInt:RESULT_SUCCESS]]);
}

RCT_EXPORT_METHOD(emailDeviceLog:(NSString *)emailAddress callback:(RCTResponseSenderBlock)callback)
{
    if (logFileName == nil) {
        dispatch_sync(dispatch_get_main_queue(), ^{
            UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"Device Log"
                message:@"Not found device log file"
                delegate:nil
                cancelButtonTitle:@"Okay"
                otherButtonTitles:nil];
            [alert show];
        });
        callback(@[[NSNumber numberWithInt:RESULT_FAILED]]);
        return;
    }
    
    NSString *appName = [[NSBundle mainBundle] objectForInfoDictionaryKey:@"CFBundleDisplayName"];
    NSString *message = [NSString stringWithFormat:@"1. Connect the device to your Mac\n\
        2. Select the device in Finder\n\
        3. Go to Files tab\n\
        4. Select and expand %@\n\
        5. Drag %@ file and drop to a desired directory to export\n\
        6. Mail exported file to %@",
        appName, logFileName, emailAddress];
    dispatch_sync(dispatch_get_main_queue(), ^{
        UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"Device Log"
            message:message
            delegate:nil
            cancelButtonTitle:@"Okay"
            otherButtonTitles:nil];
        [alert show];
    });
    callback(@[[NSNumber numberWithInt:RESULT_SUCCESS]]);
}

@end
