//
//  ViewController.m
//  DualRelease
//
//  Created by Aaron Day on 10/23/12.
//  Copyright (c) 2012 Aaron Day. All rights reserved.
//

#import "ViewController.h"

@interface ViewController ()

@end

@implementation ViewController

@synthesize applicationTitle;

- (void) viewDidLoad
{
    [super viewDidLoad];

#ifdef DUAL_RELEASE_FREE
    applicationTitle.text = NSLocalizedString(@"applicationTitle.free", nil);
#endif
#ifdef DUAL_RELEASE_PRO
    applicationTitle.text = NSLocalizedString(@"applicationTitle.pro", nil);
#endif
}

- (void) didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
}

@end
